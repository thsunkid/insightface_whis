import cv2
from PIL import Image
import argparse
from config import get_config
import face_model
from utils import load_facebank, draw_box_name, prepare_facebank

if __name__ == '__main__':
    parser = argparse.ArgumentParser(description='face verify')
    parser.add_argument('--image-size', default='112,112', help='')
    parser.add_argument('--model', default='../models/model-y1-test2/model,0', help='path to load model.')
    parser.add_argument('--ga-model', default='', help='path to load model.')
    parser.add_argument('--gpu', default=-1, type=int, help='gpu id')
    parser.add_argument('--det', default=0, type=int, help='mtcnn option, 1 means using R+O, 0 means detect from begining')
    parser.add_argument('--flip', default=0, type=int, help='whether do lr flip aug')
    parser.add_argument('--threshold', default=1.24, type=float, help='ver dist threshold')
    parser.add_argument("-u", "--update", help="whether perform update the facebank",action="store_true")    
    parser.add_argument("-s", "--save", help="whether save",action="store_true")
    parser.add_argument("-c", "--score",default=True, help="whether show the confidence score",action="store_true")

    args = parser.parse_args()

    conf = get_config()
    model = face_model.FaceModel(args)

    
    if args.update:
        targets, names = prepare_facebank(conf, model)
        print('facebank updated')
    else:
        targets, names = load_facebank(conf)
        print('facebank loaded')

    # inital camera
    cap = cv2.VideoCapture(0)
    cap.set(3,1280)
    cap.set(4,720)
    if args.save:
        video_writer = cv2.VideoWriter(str(conf.data_path/'recording.mov'), cv2.VideoWriter_fourcc('m', 'p', '4', 'v'),6, (1280,720))
    while cap.isOpened():
        isSuccess,frame = cap.read()
        if isSuccess:            
            try:
                bboxes, faces = model.get_input_2(frame)
                # bboxes = bboxes[:,:-1] #shape:[10,4],only keep 10 highest possibiity faces
                bboxes = bboxes.astype(int)
                bboxes = bboxes + [-1,-1,1,1] # personal choice    
                bboxes = [bboxes] # just have one bb
                results, score = model.infer(faces, targets)

                for idx,bbox in enumerate(bboxes):
                    if args.score:
                        frame = draw_box_name(bbox, names[results[idx] + 1] + '_{:.2f}'.format(score[idx]), frame)
                    else:
                        frame = draw_box_name(bbox, names[results[idx] + 1], frame)
            
            except:
                print('detect error')    
                continue
                
            cv2.imshow('face Capture', frame)

        if args.save:
            video_writer.write(frame)

        if cv2.waitKey(1)&0xFF == ord('q'):
            break

    cap.release()
    if args.save:
        video_writer.release()
    cv2.destroyAllWindows()    
