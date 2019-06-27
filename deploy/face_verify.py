import cv2
from PIL import Image
import argparse
from config import get_config
import face_model
from utils import load_facebank, draw_box_name, prepare_facebank
import time

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='face verify')
    parser.add_argument("-u", "--update", help="whether perform update the facebank",action="store_true")    
    parser.add_argument("-s", "--save", help="whether save",action="store_true")
    parser.add_argument("-c", "--score",default=True, help="whether show the confidence score",action="store_true")

    args = parser.parse_args()

    conf = get_config()
    model = face_model.FaceModel(conf)
    
    if args.update:
        targets, names = prepare_facebank(conf, model)
        print('facebank updated')
    else:
        targets, names = load_facebank(conf)
        print('facebank loaded')

    # inital camera
    cap = cv2.VideoCapture(0)
    if args.save:
        video_writer = cv2.VideoWriter(str(conf.data_path/'recording.mov'), cv2.VideoWriter_fourcc('m', 'p', '4', 'v'),6, (1280,720))
    while cap.isOpened():
        start_time = time.time()
        _, frame = cap.read()
        temp = frame.copy()
        frame_resized = cv2.resize(temp, None, fx=conf.resize_ratio, fy=conf.resize_ratio)
        bboxess, facess = model.find_faces(frame_resized, frame, conf)
        for bboxes, faces in zip(bboxess, facess):
            
            bboxes = bboxes + [-1,-1,1,1] # personal choice    
            bboxes = [bboxes] # just have one bb
            results, score = model.infer(faces, targets)
            for idx, bbox in enumerate(bboxes):
                if args.score:
                    frame = draw_box_name(bbox, names[results[idx] + 1] + '_{:.2f}'.format(score[idx]), frame)
                else:
                    frame = draw_box_name(bbox, names[results[idx] + 1], frame)
            cv2.imshow('jlkdsa', faces[0])
        cv2.putText(frame,'FPS: ' + str(1.0 / (time.time() - start_time)),(50,50)
            , cv2.FONT_HERSHEY_SIMPLEX, 1, (0 ,0 ,255),2,cv2.LINE_AA)
        cv2.imshow('face Capture', frame)
        if args.save:
            video_writer.write(frame)
        if cv2.waitKey(1)&0xFF == ord('q'):
            break
        
    cap.release()
    if args.save:
        video_writer.release()

    cv2.destroyAllWindows()    
