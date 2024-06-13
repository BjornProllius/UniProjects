# ------------------------------------------------------------
# 
# 
# Usage: make  a3p1  // compile program a3p1
#        make  a3p2  // compile program a3p2
#        make  a3p3  // compile program a3p3-drop
#        make  tar      // create a 'tar.gz' archive of 'allFiles'
#        make  clean     // remove unneeded files
# ------------------------------------------------------------

target=			prollius-a3
allFiles=       makefile a3p1.c a3p2.c a3p3-drop.c \
				server_logic_p1.c server_logic_p2.c \
				client_logic_p1.c client_logic_p2.c \
				server_logic_p1.h server_logic_p2.h \
				client_logic_p1.h client_logic_p2.h \
				a3p1-w24-ex1.dat a3p2-w24-ex1 \
				2mb-examplefile-com.txt prollius.pdf
# ------------------------------------------------------------


all:  a3p1  a3p2  a3p3


a3p1:  a3p1.c
	gcc -Wall a3p1.c client_logic_p1.c server_logic_p1.c -o a3p1

a3p2:  a3p2.c
	gcc -Wall a3p2.c client_logic_p2.c server_logic_p2.c -o a3p2

a3p3:  a3p3-drop.c
	gcc -Wall a3p3-drop.c -o a3p3

tar:
	touch $(target).tar.gz
	mv $(target).tar.gz  x$(target).tar.gz
	tar -cvf $(target).tar $(allFiles)
	gzip $(target).tar


clean:
	rm -f *~ out.* a3p1 a3p2 a3p3
