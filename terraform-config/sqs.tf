resource "aws_sqs_queue" "ingestion_queue" {
  name                      = var.queue_name
  visibility_timeout_seconds = 1200
}

data "aws_caller_identity" "current" {}


# 🔑 THIS enables S3 → SQS
resource "aws_sqs_queue_policy" "allow_s3_events" {
  queue_url = aws_sqs_queue.ingestion_queue.id

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Sid    = "AllowS3SendMessage"
        Effect = "Allow"

        Principal = {
          Service = "s3.amazonaws.com"
        }

        Action   = "sqs:SendMessage"
        Resource = aws_sqs_queue.ingestion_queue.arn

        Condition = {
          ArnEquals = {
            "aws:SourceArn" = aws_s3_bucket.kyc_bucket.arn
          }
          StringEquals = {
            "aws:SourceAccount" = data.aws_caller_identity.current.account_id
          }
        }
      }
    ]
  })
}

