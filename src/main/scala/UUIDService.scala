//NOTE: would rather just use util.UUID and mock it in tests, but we don't know how to pass UUID to our AccountService constructor
class UUIDService {
  def generate =
    java.util.UUID.randomUUID()
}
