dev : clean
	mkdir -p ext
	cp -R resources/shared/* resources/dev/* ext/
	lein cljsbuild once

fig : clean
	mkdir -p ext
	cp -R resources/shared/* resources/dev/* ext/
	lein cljsbuild once
	lein figwheel

prod : clean
	mkdir -p ext
	cp -R resources/shared/* resources/prod/* ext/
	lein with-profile prod cljsbuild once
	rm -rf ext/js/generated/out-*

package : clean prod
	(cd ext; zip -r looped-in.zip * -x "*.DS_Store")
	mkdir -p dist
	mv ext/looped-in.zip dist

clean :
	rm -rf resources/dev/js/generated
	rm -rf resources/prod/js/generated
	rm -rf ext
	rm -rf dist
