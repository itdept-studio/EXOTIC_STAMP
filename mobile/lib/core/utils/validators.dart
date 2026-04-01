class Validators {
  static bool isNotEmpty(String value) => value.trim().isNotEmpty;

  static bool isValidEmail(String value) {
    final normalized = value.trim();
    if (normalized.isEmpty) {
      return false;
    }
    return RegExp(r'^[^\s@]+@[^\s@]+\.[^\s@]+$').hasMatch(normalized);
  }

  static bool isValidPhone(String value) {
    final normalized = value.replaceAll(RegExp(r'\s+'), '');
    return RegExp(r'^\d{8,15}$').hasMatch(normalized);
  }

  static bool isValidPassword(String value) => value.length >= 6;
}
