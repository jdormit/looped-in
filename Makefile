dev :
	lein cljsbuild once
	rm -rf ext/js/generated/out-*

prod :
	lein with-profile prod cljsbuild once
	rm -rf ext/js/generated/out-*

package : clean prod
	(cd ext; zip -r looped-in.zip * -x "*.DS_Store")
	mkdir -p dist
	mv ext/looped-in.zip dist

clean :
	rm -rf ext/js/generated
	rm -rf dist
