from django.shortcuts import render
from django.utils import timezone
from .models import Post
from rest_framework import viewsets
from .serializers import PostSerializer
from blog import serializers

from django.contrib.auth.models import User

import firebase_admin
from firebase_admin import credentials, messaging
from django.http import JsonResponse
import os


cred_path = "firebase_key.json"

if not firebase_admin._apps:
    if os.path.exists(cred_path):
        cred = credentials.Certificate(cred_path)
        firebase_admin.initialize_app(cred)
    else:
        print("🚨 에러: firebase_key.json 파일을 찾을 수 없습니다!")


# Create your views here.
class blogImage(viewsets.ModelViewSet):
    queryset=Post.objects.all()
    serializer_class=PostSerializer

    def perform_create(self, serializer):
        # 1. DB 저장 (이미 엣지에서 admin으로 인증해서 보내므로 request.user 사용)
        try:
            admin_user = User.objects.get(pk=1) 
        except:
            admin_user = User.objects.first() # 1번 없으면 아무나 첫 번째 유저 선택

        # 작성자를 강제로 지정해서 저장
        instance = serializer.save(author=admin_user)
        # 2. 저장된 데이터 꺼내기
        title = instance.title
        body = instance.text
        
        # 이미지 URL 만들기 (에뮬레이터: 10.0.2.2 / 실제폰: 서버IP)
        image_url = f"http://10.0.2.2:8000{instance.image.url}" if instance.image else None

        target_token = "eeT5sTQ6SgSxU2tEmvrFs8:APA91bEvw2HqaSWEsyEBqoGRpGXgViZ4uZYbzL_amUnaz15brpb1Y2MxitMCKxZ3PkLEgmffirUiPfgN6pCOCxRJKcHJ8-2A3BVeRZ3GV2bBpjYxJZmAot4"
        
        if target_token:
            try:
                message = messaging.Message(
                    notification=messaging.Notification(
                        title=title,
                        body=body,
                        image=image_url,
                    ),
                    token=target_token,
                )
                messaging.send(message)
                print("🚀 알림 전송 성공!")
            except Exception as e:
                print(f"🔥 알림 전송 실패: {e}")




def post_list(request):
    posts = Post.objects.filter(published_date__lte=timezone.now()).order_by('published_date')
    return render(request,"blog/post_list.html",{'posts': posts})
    
def post_detail(request, pk):
    post = get_object_or_404(Post, pk=pk)
    return render(request,"blog/post.detail.html",{'post':post})


def post_new(request):
    if request.method =="POST":
        form = PostForm(request.POST)
        if form.is_valid():
            post= form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    else:
        form= PostForm()

    return render(request, "blog/post_edit.html",{'form':form})
    
def post_edit(request, pk):
    post = get_object_or_404(Post, pk=pk)
    if request.method =="POST":
        form = PostForm(request.POST, instance=post)
        if form.is_valid():
            post= form.save(commit=False)
            post.author = request.user
            post.published_date = timezone.now()
            post.save()
            return redirect('post_detail', pk=post.pk)
    
    else:
        form= PostForm(instance=post)
    
    return render(request,'blog/post_edit.html',{'form': form})



def test_fcm_push(request):
    
    target_token = "eeT5sTQ6SgSxU2tEmvrFs8:APA91bEvw2HqaSWEsyEBqoGRpGXgViZ4uZYbzL_amUnaz15brpb1Y2MxitMCKxZ3PkLEgmffirUiPfgN6pCOCxRJKcHJ8-2A3BVeRZ3GV2bBpjYxJZmAot4"

    if not target_token:
        return JsonResponse({'status': '토큰을 코드에 넣어주세요!'})

    # 보낼 메시지 내용
    title = "🚨 도난 감지!"
    body = "현관 앞에 수상한 사람이 감지되었습니다."

    # 메시지 구성
    message = messaging.Message(
        notification=messaging.Notification(
            title=title,
            body=body,
        ),
        token=target_token,
    )

    try:
        # 발송!
        response = messaging.send(message)
        return JsonResponse({'status': '성공', 'response': response})
    except Exception as e:
        return JsonResponse({'status': '실패', 'error': str(e)})


