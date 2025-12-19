import os
import cv2
import pathlib
import requests
from datetime import datetime

class TheftDetector:
    # --- 서버 설정 ---
    # PythonAnywhere 서버 주소 또는 로컬 서버 주소('http://' 포함)를 사용하세요.
    # HOST = "http://hsh019.pythonanywhere.com/"
    HOST = "http://127.0.0.1:8000" # 로컬 테스트 시
    USERNAME = 'admin'
    PASSWORD = '1q2w3e4r'
    # --------------------------

    # --- 상태 정의 ---
    STATE_IDLE = 0               # 아무것도 없는 상태
    STATE_PERSON_ONLY = 1        # 사람만 있는 상태
    STATE_PACKAGE_ONLY = 2       # 상자만 있는 상태
    STATE_PERSON_AND_PACKAGE = 3 # 사람과 상자가 함께 있는 상태
    
    # 상태 이름을 로그에 출력하기 위한 딕셔너리
    STATE_NAMES = {
        STATE_IDLE: "대기",
        STATE_PERSON_ONLY: "사람만 감지",
        STATE_PACKAGE_ONLY: "상자만 감지",
        STATE_PERSON_AND_PACKAGE: "사람과 상자 동시 감지"
    }
    # -------------------------

    def __init__(self, save_dir='runs/detect'):
        """
        감지기 초기화. 상태를 '대기'로 설정하고 서버 인증을 수행합니다.
        """
        self.token = None
        self.current_state = self.STATE_IDLE
        self.save_dir = pathlib.Path(save_dir)
        
        self._authenticate()
        print(f"초기 상태: {self.STATE_NAMES[self.current_state]}")

    def _authenticate(self):
        """서버에 로그인하여 인증 토큰을 받아옵니다."""
        if not self.HOST:
            print("HOST가 설정되지 않아 서버 인증을 건너뜁니다.")
            return
        print("서버 인증 중...")
        try:
            # 주소 끝에 '/'가 중복되지 않도록 처리
            res = requests.post(f"{self.HOST.rstrip('/')}/api-token-auth/", {
                "username": self.USERNAME,
                'password': self.PASSWORD,
            }, timeout=10)
            res.raise_for_status()
            self.token = res.json().get('token')
            if self.token:
                print("서버 인증 성공.")
            else:
                print("서버 인증 실패: 응답에 토큰이 없습니다.")
        except requests.exceptions.RequestException as e:
            print(f"서버 인증 오류: {e}")
            self.token = None

    def _get_current_scene_state(self, person_results, package_results):
        """탐지 결과를 바탕으로 현재 장소의 상태를 결정합니다."""
        # NMS가 적용된 후의 텐서 길이를 확인하여 객체 유무를 판단합니다.
        has_person = len(person_results) > 0
        has_package = len(package_results) > 0

        if has_person and has_package:
            return self.STATE_PERSON_AND_PACKAGE
        elif has_person:
            return self.STATE_PERSON_ONLY
        elif has_package:
            return self.STATE_PACKAGE_ONLY
        else:
            return self.STATE_IDLE

    def check_events(self, person_results, package_results, frame):
        """
        장소의 상태를 갱신하고, 상태 변화에 따라 '도난' 또는 '택배 도착' 이벤트를 감지합니다.
        """
        new_state = self._get_current_scene_state(person_results, package_results)

        # 상태 변화가 없으면 아무것도 하지 않음
        if new_state == self.current_state:
            return

        previous_state_name = self.STATE_NAMES[self.current_state]
        new_state_name = self.STATE_NAMES[new_state]
        print(f"상태 변경: {previous_state_name} -> {new_state_name}")

        # --- 상태 변화에 따른 이벤트 감지 ---
        
        # 1. 도난 이벤트 감지
        # '사람과 상자가 함께 있는 상태' 였다가, 다음 프레임에서 '상자'가 사라졌을 경우
        if self.current_state == self.STATE_PERSON_AND_PACKAGE and new_state in [self.STATE_PERSON_ONLY, self.STATE_IDLE]:
            print("!!! 이벤트 감지: 도난 의심 !!!")
            self.send_alert(frame, title="도난 의심", text="사람과 상자가 함께 있다가 상자가 사라졌습니다.")
        
        # 2. 택배 도착 이벤트 감지
        # '사람과 상자가 함께 있는 상태' 였다가, 다음 프레임에서 '사람'이 사라졌을 경우
        elif self.current_state == self.STATE_PERSON_AND_PACKAGE and new_state == self.STATE_PACKAGE_ONLY:
            print("!!! 이벤트 감지: 택배 도착 !!!")
            self.send_alert(frame, title="택배 도착", text="사람이 상자를 두고 사라졌습니다.")

        # 다음 프레임을 위해 현재 상태를 갱신
        self.current_state = new_state

    def send_alert(self, image, title, text):
        """이벤트 발생 시점의 이미지를 저장하고 서버에 알림을 전송합니다."""
        if not self.token:
            print(f"'{title}' 알림 전송 불가: 서버 인증 안됨.")
            return

        now = datetime.now()
        alert_path = self.save_dir / 'event_alerts' / now.strftime('%Y-%m-%d')
        alert_path.mkdir(parents=True, exist_ok=True)
        full_path = alert_path / f"{now.strftime('%H-%M-%S-%f')}.jpg"

        try:
            dst = cv2.resize(image, dsize=(640, 480), interpolation=cv2.INTER_AREA)
            cv2.imwrite(str(full_path), dst)
            print(f"이벤트 이미지 저장 완료: {full_path}")
        except Exception as e:
            print(f"이미지 저장 오류: {e}")
            return

        headers = {"Authorization": f"JWT {self.token}"}
        iso_now = now.isoformat()
        data = {
            'author': 1,
            'title': title,
            "text": text,
            "created_date": iso_now,
            "published_date": iso_now
        }
        
        try:
            with open(full_path, 'rb') as f:
                files = {"image": f}
                res = requests.post(f"{self.HOST.rstrip('/')}/api_root/Post/", data=data, files=files, headers=headers)
            
            print(f"서버 응답 코드: {res.status_code}")
            # print(res.json())
            if res.status_code != 201:
                print(f"서버 오류 응답: {res.text}")
        except requests.exceptions.RequestException as e:
            print(f"서버 알림 전송 오류: {e}")
        except FileNotFoundError:
            print(f"오류: 전송할 이미지를 찾을 수 없습니다 - {full_path}")