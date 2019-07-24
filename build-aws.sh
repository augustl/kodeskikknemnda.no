#!/bin/bash
set -e

lein build-site
aws s3 sync dist/ s3://utviklingslandet.no/ --delete

# Optionally
# aws cloudfront create-invalidation --distribution-id E1HAXYWSPN0SDL --paths "/*"
