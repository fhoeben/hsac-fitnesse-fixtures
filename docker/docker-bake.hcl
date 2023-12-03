variable "VERSION" {
  default = "5.3.1"
}
variable "SELENIUM_VERSION" {
  default = "latest"
}
variable "TAG" {
  default = "latest"
}

// Special target: https://github.com/docker/metadata-action#bake-definition
target "docker-metadata-action" {}

target "_hsac-base-target" {
  inherits = ["docker-metadata-action"]
  args = {
    VERSION = "${VERSION}"
  }
}

group "default" {
  targets = ["base", "test", "test-with-pdf", "chrome", "chrome-with-pdf", "combine"]
}

target "base" {
  inherits = ["_hsac-base-target"]
  pull = true
  target = "base"
  tags = ["hsac/fitnesse-fixtures-test-jre11:base-${TAG}"]
}

target "test" {
  inherits = ["_hsac-base-target"]
  pull = true
  args = {
    JRE_IMAGE = "eclipse-temurin:11-jre"
  }
  target = "hsac-fixtures"
  tags = ["hsac/fitnesse-fixtures-test-jre11:${TAG}"]
}

target "test-with-pdf" {
  inherits = ["_hsac-base-target"]
  target = "hsac-fixtures-with-pdf"
  tags = ["hsac/fitnesse-fixtures-test-jre11-with-pdf:${TAG}"]
}

target "chrome" {
  inherits = ["_hsac-base-target"]
  pull = true
  args = {
    SELENIUM_IMAGE = "seleniarm/standalone-chromium:${SELENIUM_VERSION}"
  }
  target = "hsac-chrome"
  tags = ["hsac/fitnesse-fixtures-test-jre11-chrome:${TAG}"]
}

target "chrome-with-pdf" {
  inherits = ["_hsac-base-target"]
  target = "hsac-chrome-with-pdf"
  tags = ["hsac/fitnesse-fixtures-test-jre11-chrome-with-pdf:${TAG}"]
}

target "combine" {
  inherits = ["_hsac-base-target"]
  pull = true
  args = {
    GRAALVM_IMAGE = "ghcr.io/graalvm/native-image:latest"
    BUSYBOX_IMAGE = "busybox:latest"
  }
  target = "combine"
  tags = ["hsac/fitnesse-fixtures-combine:${TAG}"]
}
