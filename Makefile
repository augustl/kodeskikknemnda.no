.DEFAULT_GOAL := all
.PHONY: build
.PHONY: upload
.PHONY: invalidate
.PHONY: invalidate-cdn

invalidate:
	aws cloudfront create-invalidation --distribution-id E39FY9Y5CBQ09S --paths "/*" --profile augustl

invalidate-cdn:
	aws cloudfront create-invalidation --distribution-id E1E5BYWITSX61Y --paths "/*" --profile augustl

build:
	lein build-site

upload:
	aws s3 sync dist/ s3://kodeskikknemnda.no/ --delete --profile augustl

all: build upload