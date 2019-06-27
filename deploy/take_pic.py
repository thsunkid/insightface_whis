import cv2
import argparse
from pathlib import Path
from PIL import Image
from datetime import datetime
import face_model
import numpy as np
import config


parser = argparse.ArgumentParser(description='take a picture')
parser.add_argument('--name','-n', default='unknown', type=str,help='input the name of the recording person')
args = parser.parse_args()

conf = config.get_config()
data_path = conf.facebank_path
if not data_path.exists():
    data_path.mkdir()

save_path = data_path/args.name
if not save_path.exists():
    save_path.mkdir()

cap = cv2.VideoCapture(0)
cap.set(3,1280)
cap.set(4,720)
model = face_model.FaceModel(conf)

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
