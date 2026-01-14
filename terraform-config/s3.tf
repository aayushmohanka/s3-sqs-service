resource "aws_s3_bucket" "kyc_bucket" {
  bucket = var.bucket_name
}

resource "aws_s3_bucket_notification" "s3_to_sqs" {
  bucket = aws_s3_bucket.kyc_bucket.id

  queue {
    queue_arn = aws_sqs_queue.ingestion_queue.arn
    events    = ["s3:ObjectCreated:*", "s3:ObjectRemoved:*"]
  }

  depends_on = [
    aws_sqs_queue_policy.allow_s3_events
  ]
}
