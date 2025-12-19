import requests
from PIL import Image
import pillow_heif
import os

direc='./mountain.HEIC'
pillow_heif.register_heif_opener()
img=Image.open(direc)
img.save('./mountain.jpg',format("jpeg"))


HOST="http://127.0.0.1:8000"
res= requests.post(HOST +"/api-token-auth/",{
    'username':'admin',
    'password':'1q2w3e4r', 
})

res.raise_for_status()
token= res.json()['token']
print(token)

headers={
    'Authorization':'JWT' + token,
    'Accept':'application/json'}

#Post create

data={
    'author':"1",
    'title':'제목 by code',
    'text': 'API 내용 by code',
    'created_date': '2025-05-10T18:34:00+09:00',
    'published_date': '2025-05-10T18:34:00+09:00',
}
#경로

file={'image': open('./mountain.jpg','rb')}
res=requests.post(HOST +'/api_root/Post/',data=data,files=file, headers= headers)
print(res)
print(res.json())
