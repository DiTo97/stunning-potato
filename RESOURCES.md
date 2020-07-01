# Resources for CV project

Useful resources that link novelty machine learning algorithms from computational vision (such as [YOLO](https://arxiv.org/pdf/1506.02640v5.pdf)-based) to the hardware capabilities of Android (or low-spec devices in general), in order to allow real-time processing of images captured by a (monocular) camera, through [**TensorFlow Lite**](https://www.tensorflow.org/lite) offerings.

## Dense image/video captioning

### Papers

- [Automatic description generation from Images: A survey of Models, Datasets, and Evaluation measures](http://homepages.inf.ed.ac.uk/keller/publications/jair16.pdf)

- [DenseCap](https://cs.stanford.edu/people/karpathy/densecap/)

- [Deep learning for Video captioning: A review](https://www.ijcai.org/Proceedings/2019/0877.pdf)

- [Sequence to sequence S2VT – Video to text](http://www.cs.utexas.edu/users/ml/papers/venugopalan.iccv15.pdf)
<br>Look for the repo within *Dropbox*

- [Detailed sentence generation architecture for Image semantics description](#)
<br>Look into within *Dropbox*

### Datasets

- [Visual Genome](https://visualgenome.org/)
- [MS COCO](https://cocodataset.org/#home)
- [IAPR TC-12](https://www.imageclef.org/photodata)

### Metrics

- [BLEU](https://en.wikipedia.org/wiki/BLEU)
- [ROUGE-L](https://en.wikipedia.org/wiki/ROUGE_(metric))
- [METEOR](https://en.wikipedia.org/wiki/METEOR)

### Extra

- [Parts-of-speech (POS) tagging](https://en.wikipedia.org/wiki/Part-of-speech_tagging)

## Familiar face recognition (?)

## Pre-trained models

- https://www.tensorflow.org/lite/models

These models may be imported on Firebase ML Kit with relative ease and cover some of the main estimation/classification/detection problems. Here, you can find some [benchmarks](https://www.tensorflow.org/lite/guide/hosted_models).

## Papers

These papers may shed a light on the high level end-to-end task that we are aiming for to help visually impaired people, or on a specific component within the pipeline.

- [A. Wedel, U. Franke, J. Klappstein, T. Brox, and D. Cremers. "Real-time Depth Estimation and Obstacle Detection from Monocular Video" In *Joint Pattern Recognition Symposium, 2006.*](https://www.researchgate.net/publication/221113910_Realtime_Depth_Estimation_and_Obstacle_Detection_from_Monocular_Video)
<br>Despite being a little outdated (*2006*), it gives a nice overview of the limitations of depth-maps estimation in real-time with the added bonus of an obstacle detection model built on top of that, designed to detect possibly dangerous incoming cars on the road up to 65 meters.
<br><br>
- [M. Domański et al., "Fast Depth Estimation on Mobile Platforms and FPGA Devices", 2015 3DTV-Conference: The True Vision - Capture, Transmission and Display of 3D Video (3DTV-CON), Lisbon, 2015, pp. 1-4, doi: 10.1109/3DTV.2015.7169365.](http://www.multimedia.edu.pl/publications/Fast-Depth-Estimation-on-Mobile-Platforms-and-FPGA-Devices.pdf)
<br>It illustrates a somewhat recent (*2015*) approach to fast depth-maps estimation in real-time on low-spec mobile platforms or FPGA devices, with an eye both on accuracy (average bad-pixel ratio or *BPR*) and on time (frames-per-second or *FPS*) under different settings of stress.

## Guides

These guides aim at providing a step-by-step walkthrough of TensorFlow capabilities.

- [Real-time Object Detection on Android using TensorFlow](https://medium.com/mindorks/detection-on-android-using-tensorflow-a3f6fe423349), which can also be found [here](https://www.youtube.com/watch?v=0oBequpSGXM) in video form on *YouTube*.
<br>It explains in a few steps how easy it is to set up real-time object detection on an Android device, given a TensorFlow detector of some kind based on *YOLO*. The written form is especially compelling, as it shows how to build Python/C++ code and make it accessible directly within Android's ecosytem. Indeed, since it implements an enhanced version of an [example](https://github.com/tensorflow/tensorflow/tree/master/tensorflow/examples/android) app provided by the TensorFlow team, it builds the detector upon the full TensorFlow library, instead of its Lite counterpart specifically tailored for mobile development.
<br><br>
- [Simple Depth Estimation from Multiple Images in Tensorflow](https://ijdykeman.github.io/slam/2019/04/07/simple-depth-from-motion.html)
<br>It proposes a simple, yet effective, approach towards depth estimation in real-time with TensorFlow, going into the details of the mathematical building blocks behid the few lines of code that allow TensorFlow to do so.
<br><br>
- [Real-time Person Segmentation in the Browser with TensorFlow.js](https://blog.tensorflow.org/2019/11/updated-bodypix-2.html)
<br>It explains how to utilize **BodyPix 2.0**, released last fall  by Google, in order to compute real-time person segmentation from a camera-captured image within a browser (it's been built upon TensorFlow.js library). Still, though, it's taken multi-person detection to the next level, as it carries an accuracy down to the joints level up to a stunning 24 body parts, hence it may still be worth a look to check if it is any re-usable by Android.

## Ready-to-use examples

These examples have to be intended either as ready-made sample Android apps (with source code), that we could take as a baseline to draw inspiration from, or as concrete implementations of deep CNN architectures, built on non-Java libraries (such as, OpenCV, PyTorch, etc...), that allow some kind of real-time processing.

- [TensorFlow Lite sample applications](https://github.com/tensorflow/examples/tree/master/lite)
<br>It is a repository of sample mobile apps on TensorFlow Lite, offered by the TensorFlow team.
<br><br>
- [PyDNet on mobile devices](https://github.com/FilippoAleotti/mobilePydnet)
<br>It is an implementation of PyDNet trained on the [**Matterport3D**](https://github.com/niessner/Matterport) indoor dataset, presented at the Intelligent Robots and Systems (IROS) conference, to perform real-time unsupervised monocular depth estimation on mobile CPUs.
<br><br>
- [Stero and sparse Depth Fusion](https://github.com/ShreyasSkandanS/stereo_sparse_depth_fusion)
<br>It is an OpenCV implementation of the method proposed in the paper ["Real-time Dense Depth Estimation by Fusing Stereo with Sparse Depth Measurements"](https://ieeexplore.ieee.org/abstract/document/8794023) from *2019*, trained on the KITTI dataset.
<br><br>
- [MVDepthNet](https://github.com/HKUST-Aerial-Robotics/MVDepthNet)
<br>It is a PyTorch implementation of the method proposed in the paper ["MVDepthNet: Real-time Multiview Depth Estimation Neural Network"](https://arxiv.org/abs/1807.08563) from *2018*, trained on the SUN3D dataset.

## Addendum

The next few lines may contain literally *anything*, ranging from proposed methods, libraries, examples, etc..., which we know little to nothing about, but that we sense could still turn out to be useful.

- [Mask R-CNN](https://arxiv.org/abs/1703.06870) (also [here](https://www.youtube.com/watch?v=s8Ui_kV9dhw) for a nice benchmark comparison with YOLO-based approaches)
<br>It is a refined version of *Faster R-CNN*, proposed in mid 2018, which doesn't stop at simple region-based object segmentation as its predecessor does, but also throws in the mix an extra convolutional branch side by side with bounding-box regression and  object classification, that allows the CNN to perform pixel-level object segmentation.

### Chieko Asakawa stuff

- [YouTube video](https://www.youtube.com/watch?v=1d-Wxf1b55o)
- [Laboratory and activities](https://www.cs.cmu.edu/~NavCog/index.html)

### Firebase ML Kit modules

- [General guide](https://towardsdatascience.com/ml-kit-for-firebase-features-capabilities-pros-and-cons-a182b4299cc)
- [Face detection](https://firebase.google.com/docs/ml-kit/detect-faces)
- [Object detection and tracking](https://firebase.google.com/docs/ml-kit/object-detection)

### Automatic description generation

- http://homepages.inf.ed.ac.uk/keller/publications/jair16.pdf

### Real-time object detection

- [YOLO](https://pjreddie.com/darknet/yolo/)

### Dense image captioning

- https://cs.stanford.edu/people/karpathy/densecap/

### Advances in Visual computing

- https://mega.nz/file/JEUFhYiQ#XaQseqt4Nl7qgZBmqAEd90ToQ-yMM3SBmNdTyG4A_vI

### Familiar face recognition

- [How robust is familiar face recognition? A repeat detection study of more than 1000 faces](https://www.researchgate.net/publication/325445147_How_robust_is_familiar_face_recognition_A_repeat_detection_study_of_more_than_1000_faces)
- https://www.sciencedirect.com/science/article/pii/0925231293E0052F
- https://www.tandfonline.com/doi/abs/10.1080/09658210902976969?journalCode=pmem20
