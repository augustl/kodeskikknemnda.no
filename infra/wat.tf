provider "aws" {
  profile = "default"
  region = "eu-west-1"
}

terraform {
  backend "s3" {
    bucket = "terraform-state-augustl"
    key = "kodeskikknemnda/terraform.tfstate"
    region = "eu-west-1"
    dynamodb_table = "terraform-locks-augustl"
  }
}

resource "aws_cloudfront_origin_access_identity" "ksn_web_origin_access_identity" {
}


resource "aws_s3_bucket" "ksn_web" {
  bucket = "kodeskikknemnda.no"
  acl = "public-read"
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::kodeskikknemnda.no/*"
        }
    ]
}
EOF

  website {
    index_document = "index.html"
  }
}


resource "aws_s3_bucket" "ksn_web_www" {
  bucket = "www.kodeskikknemnda.no"
  acl = "public-read"

  website {
    redirect_all_requests_to = "https://kodeskikknemnda.no"
  }
}


resource "aws_s3_bucket" "ksn_cdn" {
  bucket = "cdn.kodeskikknemnda.no"
  acl = "public-read"
  policy = <<EOF
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "PublicReadGetObject",
            "Effect": "Allow",
            "Principal": "*",
            "Action": "s3:GetObject",
            "Resource": "arn:aws:s3:::cdn.kodeskikknemnda.no/*"
        }
    ]
}
EOF

  website {
    index_document = "index.html"
  }
}


provider "aws" {
  region = "us-east-1"
  alias = "aws_us_east_1"
}

resource "aws_acm_certificate" "cert" {
  domain_name = "kodeskikknemnda.no"
  validation_method = "DNS"
  subject_alternative_names = ["*.kodeskikknemnda.no"]
  provider = aws.aws_us_east_1

  lifecycle {
    create_before_destroy = true
  }
}

data "aws_route53_zone" "zone" {
  name = "kodeskikknemnda.no"
  private_zone = false
}

resource "aws_route53_record" "cert_validation" {
  name = aws_acm_certificate.cert.domain_validation_options.0.resource_record_name
  type = aws_acm_certificate.cert.domain_validation_options.0.resource_record_type
  zone_id = data.aws_route53_zone.zone.id
  records = [aws_acm_certificate.cert.domain_validation_options.0.resource_record_value]
  ttl = 60
}

resource "aws_acm_certificate_validation" "cert" {
  certificate_arn = aws_acm_certificate.cert.arn
  validation_record_fqdns = [aws_route53_record.cert_validation.fqdn]
  provider = aws.aws_us_east_1
}

resource "aws_cloudfront_distribution" "ksn_web" {
  enabled = true
  is_ipv6_enabled = true
  price_class = "PriceClass_200"
  http_version = "http2"

  origin {
    domain_name = aws_s3_bucket.ksn_web.website_endpoint
    origin_id = "origin-${aws_s3_bucket.ksn_web.id}"

    custom_origin_config {
      http_port = 80
      https_port = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols = ["TLSv1.2"]
    }
  }
  default_root_object = "index.html"

  default_cache_behavior {
    allowed_methods = ["GET", "HEAD", "DELETE", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods = ["GET", "HEAD"]

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    min_ttl = "0"
    default_ttl = "300"  //3600
    max_ttl = "1200" //86400
    target_origin_id = "origin-${aws_s3_bucket.ksn_web.id}"

    // This redirects any HTTP request to HTTPS. Security first!
    viewer_protocol_policy = "redirect-to-https"
    compress = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn = aws_acm_certificate.cert.arn
    ssl_support_method = "sni-only"
  }

  aliases = ["kodeskikknemnda.no"]
}

resource "aws_route53_record" "dns_web" {
  zone_id = data.aws_route53_zone.zone.id
  name    = "kodeskikknemnda.no"
  type    = "A"

  alias {
    name = aws_cloudfront_distribution.ksn_web.domain_name
    zone_id  = aws_cloudfront_distribution.ksn_web.hosted_zone_id
    evaluate_target_health = true
  }

  depends_on = [aws_cloudfront_distribution.ksn_web]
}

resource "aws_cloudfront_distribution" "ksn_web_www" {
  enabled = true
  is_ipv6_enabled = true
  price_class = "PriceClass_200"
  http_version = "http2"

  origin {
    domain_name = aws_s3_bucket.ksn_web_www.website_endpoint
    origin_id = "origin-${aws_s3_bucket.ksn_web_www.id}"

    custom_origin_config {
      http_port = 80
      https_port = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols = ["TLSv1.2"]
    }
  }

  // Must be blank for redirect to work
  default_root_object = ""

  default_cache_behavior {
    allowed_methods = ["GET", "HEAD", "DELETE", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods = ["GET", "HEAD"]

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    min_ttl = "0"
    default_ttl = "300"  //3600
    max_ttl = "1200" //86400
    target_origin_id = "origin-${aws_s3_bucket.ksn_web_www.id}"

    // This redirects any HTTP request to HTTPS. Security first!
    viewer_protocol_policy = "redirect-to-https"
    compress = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn = aws_acm_certificate.cert.arn
    ssl_support_method = "sni-only"
  }

  aliases = ["www.kodeskikknemnda.no"]
}

resource "aws_route53_record" "dns_web_www" {
  zone_id = data.aws_route53_zone.zone.id
  name    = "www.kodeskikknemnda.no"
  type    = "A"

  alias {
    name = aws_cloudfront_distribution.ksn_web_www.domain_name
    zone_id  = aws_cloudfront_distribution.ksn_web_www.hosted_zone_id
    evaluate_target_health = true
  }

  depends_on = [aws_cloudfront_distribution.ksn_web_www]
}


resource "aws_cloudfront_distribution" "ksn_cdn" {
  enabled = true
  is_ipv6_enabled = true
  price_class = "PriceClass_200"
  http_version = "http2"

  origin {
    domain_name = aws_s3_bucket.ksn_cdn.website_endpoint
    origin_id = "origin-${aws_s3_bucket.ksn_cdn.id}"

    custom_origin_config {
      http_port = 80
      https_port = 443
      origin_protocol_policy = "http-only"
      origin_ssl_protocols = ["TLSv1.2"]
    }
  }

  default_root_object = "index.html"

  default_cache_behavior {
    allowed_methods = ["GET", "HEAD", "DELETE", "OPTIONS", "PATCH", "POST", "PUT"]
    cached_methods = ["GET", "HEAD"]

    forwarded_values {
      query_string = false

      cookies {
        forward = "none"
      }
    }

    min_ttl = "0"
    default_ttl = "300"  //3600
    max_ttl = "1200" //86400
    target_origin_id = "origin-${aws_s3_bucket.ksn_cdn.id}"

    // This redirects any HTTP request to HTTPS. Security first!
    viewer_protocol_policy = "redirect-to-https"
    compress = true
  }

  restrictions {
    geo_restriction {
      restriction_type = "none"
    }
  }

  viewer_certificate {
    acm_certificate_arn = aws_acm_certificate.cert.arn
    ssl_support_method = "sni-only"
  }

  aliases = ["cdn.kodeskikknemnda.no"]
}

resource "aws_route53_record" "dns_cdn" {
  zone_id = data.aws_route53_zone.zone.id
  name    = "cdn.kodeskikknemnda.no"
  type    = "A"

  alias {
    name = aws_cloudfront_distribution.ksn_cdn.domain_name
    zone_id  = aws_cloudfront_distribution.ksn_cdn.hosted_zone_id
    evaluate_target_health = true
  }

  depends_on = [aws_cloudfront_distribution.ksn_cdn]
}
