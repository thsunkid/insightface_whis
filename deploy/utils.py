from datetime import datetime
from PIL import Image
import numpy as np
import io
from torchvision import transforms as trans
import torch
import cv2

def l2_norm(input,axis=1):
    norm = torch.norm(input,2,axis,True)
    output = torch.div(input, norm)
    return output

def prepare_facebank(conf, model, tta = False):
    embeddings =  []
    names = ['Unknown']
    for path in conf.facebank_path.iterdir():
        if path.is_file():
            continue
        else:
            embs = []
            for file in path.iterdir():
                if not file.is_file():
                    continue
                else:
                    try:
                        img = cv2.imread(str(file))
                        img = cv2.cvtColor(img, cv2.COLOR_BGR2RGB)
                        img = np.transpose(img, (2,0,1))
                    except:
                        print('except')
                        print(file)
                        continue
                    if img.shape != (3,112, 112):
                        print("Use MTCNN to extract face!")
                        # feed to MTCNN
                        img = model.get_input(img)
                    with no_grad():
                        if tta:
                            mirror = trans.functional.hflip(img)
                            emb = torch.from_numpy(model.get_feature(img))
                            emb_mirror = torch.from_numpy(model.get_feature(mirror))
                            embs.append(l2_norm(emb + emb_mirror))
                        else:
                            emb = torch.from_numpy(model.get_feature(img))
                            emb = emb.reshape(1,emb.shape[0])
                            embs.append(emb)
        if len(embs) == 0:
            continue
        embedding = torch.cat(embs).mean(0,keepdim=True)
        embeddings.append(embedding)
        names.append(path.name)
    embeddings = torch.cat(embeddings)
    names = np.array(names)
    torch.save(embeddings, str(conf.facebank_path/'facebank.pth'))
    np.save(conf.facebank_path/'names', names)
    return embeddings, names

def load_facebank(conf):
    embeddings = torch.load(str(conf.facebank_path/'facebank.pth'))
    names = np.load(conf.facebank_path/'names.npy')
    return embeddings, names


def draw_box_name(bbox,name,frame):
    frame = cv2.rectangle(frame,(bbox[0],bbox[1]),(bbox[2],bbox[3]),(0,0,255),6)
    frame = cv2.putText(frame,
                    name,
                    (bbox[0],bbox[1]), 
                    cv2.FONT_HERSHEY_TRIPLEX, 
                    1,
                    (100,255,0),
                    3,
                    cv2.LINE_AA)
    return frame
