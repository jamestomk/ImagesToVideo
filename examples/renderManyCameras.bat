@echo off

rem this will loop to call 3dsmaxcmd to render several camera angles into images files of a resolution defined in an RPS file
rem and then use 'imagesToVideo' to produce an AVI file per rendering batch

set BASE=C:\tmp\render\water lilies sculpture
set PROJECT=C:\Users\James\Documents\3dsMax\projects\art
set SFX=.jpg
set preset=%PROJECT%\renderPresets\ARThiRes.rps
set scene=%PROJECT%\scenes\scene1.max
set startFrame=50
set endFrame=300
set nthFrame=1
set width=1440
set height=1080

set sceneState=nonCeiling focus

for %%A in ( 
CameraWiderView
AnimatedCamera
PhysCameraAbove
PreviewCamera002
PreviewCamera003
PreviewCamera004
PreviewCamera005
 ) do (
set camera=%%A
call:3dsmaxcmdFunc
)

set sceneState=Ceiling focus
set camera=PhysCameraBelow
call:3dsmaxcmdFunc
set sceneState=nonCeiling focus
 
call C:\Users\James\Documents\bin\beep.ahk
goto:eof
	
:3dsmaxcmdFunc
echo Rendering Batch: %camera% 
3dsmaxcmd ^
    "%scene%" ^
    -outputName "%BASE% %camera% %SFX%" ^
    -camera %camera% -start %startFrame% -end %endFrame%  -nthFrame %nthFrame% -width %width% ^
    -height %height% -pixelAspect 1 -showRFW:0 -skipRenderedFrames:1 -sceneState "%sceneState%"  -continueOnError ^
    -preset "%preset%"
	
call C:\Users\James\Documents\bin\imagesToVideo.bat -r "%BASE% %camera% ????%SFX%"  "%BASE% %camera%.avi"
call C:\Users\James\Documents\bin\beep.ahk

goto:eof
