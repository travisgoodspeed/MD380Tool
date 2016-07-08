
all: update
	@echo "You'll need to build the image with Android Studio."

clean:

update:
	cd ../md380tools && make clean all
	cp ../md380tools/applet/experiment.bin app/src/main/res/raw/firmware.bin
dbtest:
	java -cp ./app/build/intermediates/classes/debug com.travisgoodspeed.md380tool.MD380Codeplug ~/kk4vcz.img

