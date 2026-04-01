class MockAuthStore {
  MockAuthStore._();

  static final Map<String, _MockUser> _usersByKey = {
    'demo@metrostamp.app': const _MockUser(
      fullName: 'Metro Demo',
      email: 'demo@metrostamp.app',
      password: '123456',
      phoneWithDialCode: '+84 901234567',
    ),
    'demo': const _MockUser(
      fullName: 'Metro Demo',
      email: 'demo@metrostamp.app',
      password: '123456',
      phoneWithDialCode: '+84 901234567',
      usernameAlias: 'demo',
    ),
  };

  static String? register({
    required String fullName,
    required String email,
    required String password,
    String? phoneWithDialCode,
  }) {
    final normalizedEmail = email.trim().toLowerCase();
    if (_usersByKey.containsKey(normalizedEmail)) {
      return 'Email này đã được đăng ký.';
    }

    if (phoneWithDialCode != null && phoneWithDialCode.isNotEmpty) {
      final normalizedPhone = _normalizePhone(phoneWithDialCode);
      final phoneExists = _usersByKey.values.any(
        (user) =>
            _normalizePhone(user.phoneWithDialCode ?? '') == normalizedPhone,
      );
      if (phoneExists) {
        return 'Số điện thoại này đã được sử dụng.';
      }
    }

    final user = _MockUser(
      fullName: fullName.trim(),
      email: normalizedEmail,
      password: password,
      phoneWithDialCode: phoneWithDialCode?.trim(),
    );
    _usersByKey[normalizedEmail] = user;
    return null;
  }

  static String? login({
    required String identifier,
    required String password,
  }) {
    final normalizedIdentifier = identifier.trim().toLowerCase();
    final user = _usersByKey[normalizedIdentifier];
    if (user == null) {
      return 'Tài khoản không tồn tại. Hãy đăng ký trước.';
    }
    if (user.password != password) {
      return 'Mật khẩu không chính xác.';
    }
    return null;
  }

  static String _normalizePhone(String value) {
    return value.replaceAll(RegExp(r'\s+'), '');
  }
}

class _MockUser {
  const _MockUser({
    required this.fullName,
    required this.email,
    required this.password,
    this.phoneWithDialCode,
    this.usernameAlias,
  });

  final String fullName;
  final String email;
  final String password;
  final String? phoneWithDialCode;
  final String? usernameAlias;
}
