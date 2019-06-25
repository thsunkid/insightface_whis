import cv2
import argparse
from pathlib import Path
from PIL import Image
from datetime import datetime
import face_model
import numpy as np

parser = argparse.ArgumentParser(description='take a picture')
parser.add_argument('--name','-n', default='unknown', type=str,help='input the name of the recording person')
parser.add_argument('--image-size', default='112,112', help='')
parser.add_argument('--model', default='../models/model-y1-test2/model,0', help='path to load model.')
parser.add_argument('--ga-model', default='', help='path to load model.')
parser.add_argument('--gpu', default=-1, type=int, help='gpu id')
parser.add_argument('--det', default=0, type=int, help='mtcnn option, 1 means using R+O, 0 means detect from begining')
parser.add_argument('--flip', default=0, type=int, help='whether do lr flip aug')
parser.add_argument('--threshold', default=1.24, type=float, help='ver dist threshold')
args = parser.parse_args()

data_path = Path('../facebank')/args.name
save_path = data_path
if not save_path.exists():
    save_path.mkdir()

cap = cv2.VideoCapture(0)
cap.set(3,1280)
cap.set(4,720)
model = face_model.FaceModel(args)

while cap.isOpened():

    isSuccess,frame = cap.read()
    if isSuccess:
        frame_text = cv2.putText(frame,
                    '',
                    (10,100), 
                    cv2.FONT_HERSHEY_SIMPLEX, 
                    2,
                    (0,255,0),
                    3,
                    cv2.LINE_AA)
        cv2.imshow("My Capture",frame_text)
    if cv2.waitKey(1)&0xFF == ord('t'):
        try:            
            img = model.get_input(frame)
            img = np.transpose(img,(1,2,0))
            img = cv2.cvtColor(img, cv2.COLOR_RGB2BGR)
            cv2.imwrite(str(save_path/'{}.jpg'.format(str(datetime.now())[:-7].replace(":","-").replace(" ","-"))), img)
        except:
            print('no face captured')
        
    if cv2.waitKey(1)&0xFF == ord('q'):
        break

cap.release()
cv2.destoryAllWindows()
