.DEFAULT_GOAL := all
.PHONY: build
.PHONY: upload
.PHONY: invalidate
.PHONY: invalidate-cdn

invalidate:
	aws cloudfront create-invalidation --distribution-id E39FY9Y5CBQ09S --paths "/*"

invalidate-cdn:
	aws cloudfront create-invalidation --distribution-id E1E5BYWITSX61Y --paths "/*"

build:
	lein build-site

upload:
	aws s3 sync dist/ s3://kodeskikknemnda.no/ --delete

all: build upload