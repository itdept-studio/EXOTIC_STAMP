import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import '../../../app/theme/app_colors.dart';
import '../../../app/router.dart';
import '../../../core/errors/app_exception.dart';
import '../../../core/utils/validators.dart';
import '../../../core/widgets/app_notice_dialog.dart';
import '../data/auth_api_service.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  static const List<_PhoneCountry> _phoneCountries = [
    _PhoneCountry(
      name: 'Việt Nam',
      isoCode: 'VN',
      dialCode: '+84',
    ),
    _PhoneCountry(
      name: 'Hoa Kỳ',
      isoCode: 'US',
      dialCode: '+1',
    ),
    _PhoneCountry(
      name: 'Singapore',
      isoCode: 'SG',
      dialCode: '+65',
    ),
    _PhoneCountry(
      name: 'Thái Lan',
      isoCode: 'TH',
      dialCode: '+66',
    ),
    _PhoneCountry(
      name: 'Nhật Bản',
      isoCode: 'JP',
      dialCode: '+81',
    ),
    _PhoneCountry(
      name: 'Hàn Quốc',
      isoCode: 'KR',
      dialCode: '+82',
    ),
  ];

  final TextEditingController fullNameController = TextEditingController();
  final TextEditingController usernameController = TextEditingController();
  final TextEditingController emailController = TextEditingController();
  final TextEditingController phoneController = TextEditingController();
  final TextEditingController passwordController = TextEditingController();
  final TextEditingController confirmPasswordController =
      TextEditingController();

  bool agreeToTerms = false;
  bool obscurePassword = true;
  bool obscureConfirmPassword = true;
  bool isSubmitting = false;
  _PhoneCountry selectedPhoneCountry = _phoneCountries.first;
  final AuthApiService _authApiService = AuthApiService();

  @override
  void dispose() {
    fullNameController.dispose();
    usernameController.dispose();
    emailController.dispose();
    phoneController.dispose();
    passwordController.dispose();
    confirmPasswordController.dispose();
    super.dispose();
  }

  Future<void> _showNotice({
    required String title,
    required String message,
  }) {
    return showDialog<void>(
      context: context,
      builder: (context) {
        return AppNoticeDialog(title: title, message: message);
      },
    );
  }

  Future<void> _handleRegister() async {
    if (isSubmitting) {
      return;
    }

    final fullName = fullNameController.text.trim();
    final username = usernameController.text.trim();
    final email = emailController.text.trim();
    final phone = phoneController.text.trim();
    final password = passwordController.text;
    final confirmPassword = confirmPasswordController.text;

    if (!Validators.isNotEmpty(fullName)) {
      await _showNotice(
        title: 'Thiếu họ tên',
        message: 'Vui lòng nhập họ và tên của bạn.',
      );
      return;
    }

    if (!Validators.isValidEmail(email)) {
      await _showNotice(
        title: 'Email chưa hợp lệ',
        message:
            'Vui lòng nhập đúng định dạng email, ví dụ `example@gmail.com`.',
      );
      return;
    }

    if (!Validators.isNotEmpty(username)) {
      await _showNotice(
        title: 'Thiếu tên đăng nhập',
        message: 'Vui lòng nhập tên đăng nhập để tiếp tục.',
      );
      return;
    }

    if (phone.isNotEmpty && !Validators.isValidPhone(phone)) {
      await _showNotice(
        title: 'Số điện thoại chưa hợp lệ',
        message: 'Vui lòng chỉ nhập số điện thoại gồm 8 đến 15 chữ số.',
      );
      return;
    }

    if (!Validators.isValidPassword(password)) {
      await _showNotice(
        title: 'Mật khẩu chưa đạt yêu cầu',
        message: 'Mật khẩu cần có ít nhất 6 ký tự.',
      );
      return;
    }

    if (password != confirmPassword) {
      await _showNotice(
        title: 'Mật khẩu không khớp',
        message: 'Vui lòng kiểm tra lại phần xác nhận mật khẩu.',
      );
      return;
    }

    if (!agreeToTerms) {
      await _showNotice(
        title: 'Chưa đồng ý điều khoản',
        message:
            'Bạn cần đồng ý Điều khoản dịch vụ và Chính sách bảo mật để tiếp tục.',
      );
      return;
    }

    setState(() {
      isSubmitting = true;
    });

    final fullNameParts = _splitFullName(fullName);
    final firstName = fullNameParts.$1;
    final lastName = fullNameParts.$2;
    final phoneNumber =
        phone.isEmpty ? '' : '${selectedPhoneCountry.dialCode}$phone';

    String? registerError;
    try {
      await _authApiService.register(
        firstName: firstName,
        lastName: lastName,
        username: username,
        email: email,
        phoneNumber: phoneNumber,
        password: password,
      );
    } on AppException catch (exception) {
      registerError = exception.message;
    } catch (_) {
      registerError = 'Đăng ký chưa thành công. Vui lòng thử lại.';
    }

    if (!mounted) {
      return;
    }

    setState(() {
      isSubmitting = false;
    });

    if (registerError != null) {
      await _showNotice(
        title: 'Đăng ký chưa thành công',
        message: registerError,
      );
      return;
    }

    Navigator.of(context).pushReplacementNamed(
      AppRouter.verifyEmailOtp,
      arguments: email,
    );
  }

  (String, String) _splitFullName(String fullName) {
    final parts = fullName
        .split(RegExp(r'\s+'))
        .where((item) => item.isNotEmpty)
        .toList();
    if (parts.length <= 1) {
      final fallbackName = parts.isEmpty ? fullName : parts.first;
      return (fallbackName, fallbackName);
    }

    return (parts.first, parts.sublist(1).join(' '));
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        toolbarHeight: 64,
        titleSpacing: 0,
        title: const Text(
          'Tạo tài khoản',
          style: TextStyle(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: AppColors.textPrimary,
          ),
        ),
      ),
      body: SafeArea(
        top: false,
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(20, 18, 20, 24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Center(
                child: Column(
                  children: [
                    Text(
                      'Bắt đầu ngay',
                      textAlign: TextAlign.center,
                      style: TextStyle(
                        fontSize: 28,
                        fontWeight: FontWeight.w800,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    SizedBox(height: 10),
                    Text.rich(
                      TextSpan(
                        style: TextStyle(
                          fontSize: 16,
                          height: 1.45,
                          color: AppColors.textMuted,
                          fontWeight: FontWeight.w500,
                        ),
                        children: [
                          TextSpan(text: 'Tham gia cùng với '),
                          TextSpan(
                            text: 'Exotic ',
                            style: TextStyle(
                              color: AppColors.brandBlue,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          TextSpan(
                            text: 'Stamp',
                            style: TextStyle(
                              color: AppColors.brandRed,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          TextSpan(
                            text:
                                ' và hàng ngàn người dùng khác để trải nghiệm đầy đủ dịch vụ.',
                          ),
                        ],
                      ),
                      textAlign: TextAlign.center,
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 28),
              const _RegisterLabel(text: 'Họ và Tên'),
              const SizedBox(height: 8),
              _RegisterInputField(
                hintText: 'Nguyễn Văn A',
                prefixIcon: Icons.person_outline,
                controller: fullNameController,
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(text: 'Tên đăng nhập'),
              const SizedBox(height: 8),
              _RegisterInputField(
                hintText: 'username123',
                prefixIcon: Icons.alternate_email,
                controller: usernameController,
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(text: 'Email'),
              const SizedBox(height: 8),
              _RegisterInputField(
                hintText: 'example@gmail.com',
                prefixIcon: Icons.mail_outline,
                controller: emailController,
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(
                text: 'Số điện thoại',
                trailingText: '(Tùy chọn)',
              ),
              const SizedBox(height: 8),
              _PhoneNumberField(
                selectedCountry: selectedPhoneCountry,
                countries: _phoneCountries,
                controller: phoneController,
                onCountryChanged: (country) {
                  setState(() {
                    selectedPhoneCountry = country;
                  });
                },
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(text: 'Mật khẩu'),
              const SizedBox(height: 8),
              _RegisterInputField(
                hintText: '........',
                prefixIcon: Icons.lock_outline,
                controller: passwordController,
                obscureText: obscurePassword,
                suffixIcon: IconButton(
                  onPressed: () {
                    setState(() {
                      obscurePassword = !obscurePassword;
                    });
                  },
                  icon: Icon(
                    obscurePassword
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                    color: AppColors.textMuted,
                  ),
                ),
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(text: 'Xác nhận mật khẩu'),
              const SizedBox(height: 8),
              _RegisterInputField(
                hintText: '........',
                prefixIcon: Icons.verified_user_outlined,
                controller: confirmPasswordController,
                obscureText: obscureConfirmPassword,
                suffixIcon: IconButton(
                  onPressed: () {
                    setState(() {
                      obscureConfirmPassword = !obscureConfirmPassword;
                    });
                  },
                  icon: Icon(
                    obscureConfirmPassword
                        ? Icons.visibility_outlined
                        : Icons.visibility_off_outlined,
                    color: AppColors.textMuted,
                  ),
                ),
              ),
              const SizedBox(height: 20),
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Padding(
                    padding: const EdgeInsets.only(top: 2),
                    child: SizedBox(
                      height: 24,
                      width: 24,
                      child: Checkbox(
                        value: agreeToTerms,
                        onChanged: (value) {
                          setState(() {
                            agreeToTerms = value ?? false;
                          });
                        },
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    child: RichText(
                      text: const TextSpan(
                        style: TextStyle(
                          fontSize: 15,
                          height: 1.45,
                          color: AppColors.textMuted,
                          fontWeight: FontWeight.w500,
                        ),
                        children: [
                          TextSpan(text: 'Tôi đồng ý với '),
                          TextSpan(
                            text: 'Điều khoản dịch vụ',
                            style: TextStyle(
                              color: AppColors.brandRed,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                          TextSpan(text: ' và '),
                          TextSpan(
                            text: 'Chính sách bảo mật.',
                            style: TextStyle(
                              color: AppColors.brandRed,
                              fontWeight: FontWeight.w700,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 22),
              SizedBox(
                width: double.infinity,
                height: 54,
                child: ElevatedButton(
                  onPressed: isSubmitting ? null : _handleRegister,
                  child: isSubmitting
                      ? const SizedBox(
                          width: 22,
                          height: 22,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.2,
                            color: AppColors.background,
                          ),
                        )
                      : const Text('Đăng Ký Ngay'),
                ),
              ),
              const SizedBox(height: 22),
              const Row(
                children: [
                  Expanded(child: Divider(color: AppColors.border)),
                  Padding(
                    padding: EdgeInsets.symmetric(horizontal: 12),
                    child: Text(
                      'HOẶC ĐĂNG KÝ BẰNG',
                      style: TextStyle(
                        color: AppColors.textMuted,
                        fontSize: 12,
                        fontWeight: FontWeight.w700,
                        letterSpacing: 0.6,
                      ),
                    ),
                  ),
                  Expanded(child: Divider(color: AppColors.border)),
                ],
              ),
              const SizedBox(height: 14),
              const Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  _RegisterSocialButton(
                    icon: FontAwesomeIcons.google,
                    color: AppColors.brandRed,
                  ),
                  SizedBox(width: 10),
                  _RegisterSocialButton(
                    icon: FontAwesomeIcons.facebookF,
                    color: AppColors.brandRed,
                  ),
                  SizedBox(width: 10),
                  _RegisterSocialButton(
                    icon: FontAwesomeIcons.apple,
                    color: AppColors.brandRed,
                  ),
                ],
              ),
              const SizedBox(height: 22),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text(
                    'Bạn đã có tài khoản? ',
                    style: TextStyle(
                      fontSize: 15,
                      color: AppColors.textMuted,
                      fontWeight: FontWeight.w500,
                    ),
                  ),
                  GestureDetector(
                    onTap: () {
                      Navigator.of(context)
                          .pushReplacementNamed(AppRouter.auth);
                    },
                    child: const Text(
                      'Đăng nhập ngay',
                      style: TextStyle(
                        fontSize: 15,
                        color: AppColors.brandBlue,
                        fontWeight: FontWeight.w700,
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _PhoneCountry {
  const _PhoneCountry({
    required this.name,
    required this.isoCode,
    required this.dialCode,
  });

  final String name;
  final String isoCode;
  final String dialCode;
}

class _RegisterLabel extends StatelessWidget {
  const _RegisterLabel({
    required this.text,
    this.trailingText,
  });

  final String text;
  final String? trailingText;

  @override
  Widget build(BuildContext context) {
    return RichText(
      text: TextSpan(
        style: const TextStyle(
          fontSize: 15,
          color: AppColors.textPrimary,
          fontWeight: FontWeight.w700,
        ),
        children: [
          TextSpan(text: text),
          if (trailingText != null)
            TextSpan(
              text: ' $trailingText',
              style: const TextStyle(
                color: AppColors.textMuted,
                fontWeight: FontWeight.w500,
              ),
            ),
        ],
      ),
    );
  }
}

class _PhoneNumberField extends StatelessWidget {
  const _PhoneNumberField({
    required this.selectedCountry,
    required this.countries,
    required this.controller,
    required this.onCountryChanged,
  });

  final _PhoneCountry selectedCountry;
  final List<_PhoneCountry> countries;
  final TextEditingController controller;
  final ValueChanged<_PhoneCountry> onCountryChanged;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);
    final inputTheme = theme.inputDecorationTheme;
    final enabledBorder = inputTheme.enabledBorder?.borderSide ??
        const BorderSide(color: AppColors.border);
    final borderRadius =
        (inputTheme.enabledBorder as OutlineInputBorder?)?.borderRadius ??
            BorderRadius.circular(14);
    final hintStyle = inputTheme.hintStyle ??
        const TextStyle(
          fontSize: 17,
          color: AppColors.textMuted,
          fontWeight: FontWeight.w500,
        );

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
      decoration: BoxDecoration(
        color: inputTheme.fillColor ?? AppColors.inputBackground,
        borderRadius: borderRadius,
        border: Border.fromBorderSide(enabledBorder),
      ),
      child: Row(
        children: [
          const Icon(
            Icons.phone_outlined,
            color: AppColors.brandBlue,
          ),
          const SizedBox(width: 12),
          PopupMenuButton<_PhoneCountry>(
            onSelected: onCountryChanged,
            offset: const Offset(0, 54),
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(14),
            ),
            color: AppColors.background,
            itemBuilder: (context) {
              return countries
                  .map(
                    (country) => PopupMenuItem<_PhoneCountry>(
                      value: country,
                      child: Row(
                        children: [
                          _CountryFlag(country: country),
                          const SizedBox(width: 10),
                          Text(
                            country.dialCode,
                            style: const TextStyle(
                              fontSize: 14,
                              fontWeight: FontWeight.w700,
                              color: AppColors.brandRed,
                            ),
                          ),
                          const SizedBox(width: 10),
                          Expanded(
                            child: Text(
                              country.name,
                              style: const TextStyle(
                                fontSize: 14,
                                fontWeight: FontWeight.w600,
                                color: AppColors.textPrimary,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ),
                  )
                  .toList();
            },
            child: Padding(
              padding: const EdgeInsets.symmetric(vertical: 14),
              child: Row(
                mainAxisSize: MainAxisSize.min,
                children: [
                  _CountryFlag(country: selectedCountry),
                  const SizedBox(width: 6),
                  Text(
                    selectedCountry.dialCode,
                    style: const TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                      color: AppColors.brandRed,
                    ),
                  ),
                  const SizedBox(width: 2),
                  const Icon(
                    Icons.keyboard_arrow_down_rounded,
                    size: 16,
                    color: AppColors.textMuted,
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: TextField(
              controller: controller,
              keyboardType: TextInputType.phone,
              decoration: InputDecoration(
                isCollapsed: true,
                hintText: '901 234 567',
                hintStyle: hintStyle,
                border: InputBorder.none,
                enabledBorder: InputBorder.none,
                focusedBorder: InputBorder.none,
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _CountryFlag extends StatelessWidget {
  const _CountryFlag({
    required this.country,
  });

  final _PhoneCountry country;

  static const double _width = 22;
  static const double _height = 16;

  @override
  Widget build(BuildContext context) {
    return ClipRRect(
      borderRadius: BorderRadius.circular(3),
      child: SizedBox(
        width: _width,
        height: _height,
        child: DecoratedBox(
          decoration: BoxDecoration(
            border: Border.all(color: AppColors.border.withValues(alpha: 0.8)),
          ),
          child: _buildFlag(),
        ),
      ),
    );
  }

  Widget _buildFlag() {
    switch (country.isoCode) {
      case 'VN':
        return Container(
          color: const Color(0xFFDA251D),
          alignment: Alignment.center,
          child: const Icon(
            Icons.star,
            size: _height * 0.7,
            color: Color(0xFFFFD54F),
          ),
        );
      case 'US':
        return Stack(
          children: [
            Column(
              children: List.generate(
                6,
                (index) => Expanded(
                  child: Container(
                    color:
                        index.isEven ? const Color(0xFFB22234) : Colors.white,
                  ),
                ),
              ),
            ),
            Align(
              alignment: Alignment.topLeft,
              child: Container(
                width: _width * 0.45,
                height: _height * 0.55,
                color: const Color(0xFF3C3B6E),
              ),
            ),
          ],
        );
      case 'SG':
        return Column(
          children: [
            Expanded(
              child: Container(
                color: const Color(0xFFEF3340),
                padding: const EdgeInsets.only(left: 3),
                alignment: Alignment.centerLeft,
                child: Container(
                  width: _height * 0.38,
                  height: _height * 0.38,
                  decoration: const BoxDecoration(
                    shape: BoxShape.circle,
                    color: Colors.white,
                  ),
                  child: Align(
                    alignment: const Alignment(0.35, 0),
                    child: Container(
                      width: _height * 0.22,
                      height: _height * 0.22,
                      decoration: const BoxDecoration(
                        shape: BoxShape.circle,
                        color: Color(0xFFEF3340),
                      ),
                    ),
                  ),
                ),
              ),
            ),
            Expanded(child: Container(color: Colors.white)),
          ],
        );
      case 'TH':
        return Column(
          children: [
            Expanded(child: Container(color: const Color(0xFFDA121A))),
            Expanded(child: Container(color: Colors.white)),
            Expanded(child: Container(color: const Color(0xFF241D4F))),
            Expanded(child: Container(color: Colors.white)),
            Expanded(child: Container(color: const Color(0xFFDA121A))),
          ],
        );
      case 'JP':
        return Container(
          color: Colors.white,
          alignment: Alignment.center,
          child: Container(
            width: _height * 0.65,
            height: _height * 0.65,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              color: Color(0xFFBC002D),
            ),
          ),
        );
      case 'KR':
        return Container(
          color: Colors.white,
          alignment: Alignment.center,
          child: SizedBox(
            width: _height * 0.75,
            height: _height * 0.75,
            child: Stack(
              children: [
                Align(
                  alignment: Alignment.topCenter,
                  child: Container(
                    width: _height * 0.75,
                    height: _height * 0.375,
                    decoration: const BoxDecoration(
                      color: Color(0xFFC60C30),
                      borderRadius: BorderRadius.vertical(
                        top: Radius.circular(999),
                      ),
                    ),
                  ),
                ),
                Align(
                  alignment: Alignment.bottomCenter,
                  child: Container(
                    width: _height * 0.75,
                    height: _height * 0.375,
                    decoration: const BoxDecoration(
                      color: Color(0xFF003478),
                      borderRadius: BorderRadius.vertical(
                        bottom: Radius.circular(999),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      default:
        return Container(
          color: AppColors.blueTint,
          alignment: Alignment.center,
          child: Text(
            country.isoCode,
            style: const TextStyle(
              fontSize: 8,
              fontWeight: FontWeight.w700,
              color: AppColors.brandBlue,
            ),
          ),
        );
    }
  }
}

class _RegisterInputField extends StatelessWidget {
  const _RegisterInputField({
    required this.hintText,
    required this.prefixIcon,
    required this.controller,
    this.suffixIcon,
    this.obscureText = false,
  });

  final String hintText;
  final IconData prefixIcon;
  final TextEditingController controller;
  final Widget? suffixIcon;
  final bool obscureText;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      obscureText: obscureText,
      decoration: InputDecoration(
        hintText: hintText,
        prefixIcon: Icon(
          prefixIcon,
          color: AppColors.brandBlue,
        ),
        suffixIcon: suffixIcon,
      ),
    );
  }
}

class _RegisterSocialButton extends StatelessWidget {
  const _RegisterSocialButton({
    required this.icon,
    required this.color,
  });

  final IconData icon;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 74,
      height: 52,
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: AppColors.brandBlue),
      ),
      child: Center(
        child: FaIcon(
          icon,
          size: 22,
          color: color,
        ),
      ),
    );
  }
}
