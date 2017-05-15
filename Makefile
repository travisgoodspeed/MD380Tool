
all: update
	@echo "You'll need to build the image with Android Studio."

clean:

update:
	curl http://md380.org/releases/daily/firmware-NoGPS.bin >app/src/main/res/raw/firmware.bin
	curl http://md380.org/releases/daily/firmware-GPS.bin >app/src/main/res/raw/firmware-gps.bin
dbtest:
	java -cp ./app/build/intermediates/classes/debug com.travisgoodspeed.md380tool.MD380Codeplug ~/kk4vcz.img

