## kodeskikknemnda.no

## Howto

1. Have AWS CLI 2.0 installed
2. Run `make`


## Encoding

MP3:

     ffmpeg -i ep.wav -codec:a libmp3lame -qscale:a 6 out.mp3

Youtube videos:

    ffmpeg -r 1 -loop 1 -y -i 'logo full.png' -i ep.wav -shortest -c:v libx264 -tune stillimage -pix_fmt yuv420p out.mp4