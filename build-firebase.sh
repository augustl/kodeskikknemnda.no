#!/bin/bash
set -e

lein build-site
firebase deploy
