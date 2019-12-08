.DEFAULT_GOAL := all
.PHONY: build
.PHONY: upload
.PHONY: invalidate

invalidate:
	aws cloudfront create-invalidation --distribution-id E1HAXYWSPN0SDL --paths "/*"

build:
	lein build-site

upload:
	aws s3 sync dist/ s3://utviklingslandet.no/ --delete

all: build upload