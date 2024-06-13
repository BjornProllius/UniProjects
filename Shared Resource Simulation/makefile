# ------------------------------------------------------------
# 
# 
# Usage: make  a4p1  // compile program a4p1
#        make  tar      // create a 'tar.gz' archive of 'allFiles'
#        make  clean     // remove unneeded files
# ------------------------------------------------------------

target=			prollius-a4
allFiles=       makefile a4p1.c prollius.pdf input.txt
# ------------------------------------------------------------


a4p1:  a4p1.c
	gcc -Wall a4p1.c -o a4p1


tar:
	touch $(target).tar.gz
	mv $(target).tar.gz  x$(target).tar.gz
	tar -cvf $(target).tar $(allFiles)
	gzip $(target).tar


clean:
	rm -f *~ out.* a4p1
