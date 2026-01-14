resource "aws_iam_user" "tas_user" {
  name = var.iam_user_name
}

resource "aws_iam_access_key" "tas_user_key" {
  user = aws_iam_user.tas_user.name
}

output "access_key_id" {
  value = aws_iam_access_key.tas_user_key.id
}

output "secret_access_key" {
  value     = aws_iam_access_key.tas_user_key.secret
  sensitive = true
}


resource "aws_iam_policy" "tas_policy" {
  name = "tas-ingestion-policy-dev"

  policy = jsonencode({
    Version = "2012-10-17",
    Statement = [
      {
        Effect = "Allow",
        Action = [
          "s3:GetObject",
          "s3:ListBucket",
          "s3:PutObject",
          "s3:AbortMultipartUpload",
          "s3:ListMultipartUploadParts"
        ],
        Resource = [
          aws_s3_bucket.kyc_bucket.arn,
          "${aws_s3_bucket.kyc_bucket.arn}/*"
        ]
      },
      {
        Effect = "Allow",
        Action = [
          "sqs:ReceiveMessage",
          "sqs:DeleteMessage",
          "sqs:GetQueueAttributes"
        ],
        Resource = aws_sqs_queue.ingestion_queue.arn
      }
    ]
  })
}

resource "aws_iam_user_policy_attachment" "user_policy_attach" {
  user       = aws_iam_user.tas_user.name
  policy_arn = aws_iam_policy.tas_policy.arn
}

