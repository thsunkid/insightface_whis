from easydict import EasyDict as edict
from pathlib import Path
from torchvision import transforms as trans

def get_config():
    conf = edict()
    conf.data_path = Path('../facebank')
    conf.work_path = Path('models/')
    conf.model_path = conf.work_path/'models'
    conf.log_path = conf.work_path/'log'
    conf.save_path = conf.work_path/'save'
    conf.input_size = [112,112]
    conf.embedding_size = 512
    conf.use_mobilfacenet = False
    conf.net_depth = 50
    conf.drop_ratio = 0.6
    conf.net_mode = 'ir_se' # or 'ir'

#--------------------Inference Config ------------------------
    conf.facebank_path = conf.data_path
    conf.threshold = 1.5
    conf.face_limit = 10 
    #when inference, at maximum detect 10 faces in one image, my laptop is slow
    conf.min_face_size = 30 
    # the larger this value, the faster deduction, comes with tradeoff in small faces
    return conf