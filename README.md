# InvasionDetect 프로젝트

## 프로젝트 개요

`InvasionDetect`는 YOLOv5 객체 탐지 모델을 활용하여 특정 영역에 대한 침입을 탐지하고, 결과를 웹 서비스와 모바일 애플리케이션을 통해 사용자에게 제공하는 시스템입니다.

## 프로젝트 구성

본 프로젝트는 세 가지 주요 구성 요소로 이루어져 있습니다.

1.  **`yolov5`**:
    *   객체 탐지를 위한 핵심 모델입니다.
    *   실시간으로 입력되는 영상이나 이미지를 분석하여 침입 여부를 판단합니다.
    *   `detect.py`를 사용하여 탐지를 수행하고, `train.py`를 통해 커스텀 데이터셋으로 모델을 학습시킬 수 있습니다.

2.  **`PhotoBlogServer`**:
    *   탐지된 침입 결과를 저장하고 사용자에게 보여주기 위한 Django 기반 웹 서버입니다.
    *   탐지된 이미지는 `media/` 디렉토리에 저장될 수 있습니다.
    *   사용자는 웹 브라우저를 통해 침입 기록을 확인하고 관리할 수 있습니다.

3.  **`PhotoViewer`**:
    *   `PhotoBlogServer`와 연동하여 침입 탐지 결과를 보여주는 Android 애플리케이션입니다.
    *   사용자는 모바일 환경에서 실시간으로 탐지 결과를 확인할 수 있습니다.

## 설정 및 실행 방법

### 1. `yolov5` 모델 실행

```bash
# yolov5 디렉토리로 이동
cd yolov5

# 필요한 라이브러리 설치
pip install -r requirements.txt

# 침입 탐지 실행 (카메라 소스 1번 사용 예시)
# detect222.py는 커스텀된 탐지 스크립트입니다.
python detect222.py --weights best.pt --source 1
```

### 2. `PhotoBlogServer` 서버 실행

```bash
# PhotoBlogServer 디렉토리로 이동
cd PhotoBlogServer

# 가상환경 활성화 (필요시)
source venv/bin/activate

# 필요한 라이브러리 설치 (requirements.txt 파일이 있다면)
# pip install -r requirements.txt 

# 데이터베이스 마이그레이션
python manage.py migrate

# 개발 서버 실행
python manage.py runserver
```

### 3. `PhotoViewer` 앱 빌드

*   Android Studio를 사용하여 `PhotoViewer` 디렉토리를 엽니다.
*   Gradle 동기화를 완료한 후, "Run 'app'"을 실행하여 앱을 빌드하고 기기에 설치합니다.

## 참고

*   각 디렉토리의 세부적인 설정 및 사용법은 해당 디렉토리 내의 문서를 참고하십시오.
*   모델 학습 및 탐지 옵션에 대한 자세한 내용은 `yolov5`의 `README.md` 파일을 확인하십시오.

## 디렉토리 구조

```
.
├── PhotoBlogServer/      # Django 웹 서버
│   ├── blog/             # 블로그 앱
│   ├── media/            # 업로드된 미디어 파일
│   └── mysite/           # Django 프로젝트 설정
├── PhotoViewer/          # Android 앱
│   └── app/              # Android 앱 소스 코드
├── yolov5/               # YOLOv5 모델
│   ├── data/             # 데이터셋
│   ├── models/           # 모델 정의
│   ├── runs/             # 실행 결과 (탐지, 학습 등)
│   └── utils/            # 유틸리티 스크립트
└── README.md
```
