variable "region" {
  default = "ap-south-1"
}

variable "bucket_name" {
  default = "kyc-refinitiv-files-dev"
}

variable "queue_name" {
  default = "kyc-file-events-queue-dev"
}

variable "iam_user_name" {
  default = "tas-local-user"
}

variable "iam_role_name" {
  default = "tas-ingestion-role-dev"
}
