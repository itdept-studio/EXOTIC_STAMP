import 'package:flutter/material.dart';

import '../../../app/router.dart';
import '../../../app/theme/app_colors.dart';
import '../../../core/errors/app_exception.dart';
import '../../../core/utils/validators.dart';
import '../../../core/widgets/app_notice_dialog.dart';
import '../data/auth_api_service.dart';

class ForgotPasswordPage extends StatefulWidget {
  const ForgotPasswordPage({super.key});

  @override
  State<ForgotPasswordPage> createState() => _ForgotPasswordPageState();
}

class _ForgotPasswordPageState extends State<ForgotPasswordPage> {
  static const List<_PhoneCountry> _phoneCountries = [
    _PhoneCountry(name: 'Việt Nam', isoCode: 'VN', dialCode: '+84'),
    _PhoneCountry(name: 'Hoa Kỳ', isoCode: 'US', dialCode: '+1'),
    _PhoneCountry(name: 'Singapore', isoCode: 'SG', dialCode: '+65'),
    _PhoneCountry(name: 'Thái Lan', isoCode: 'TH', dialCode: '+66'),
    _PhoneCountry(name: 'Nhật Bản', isoCode: 'JP', dialCode: '+81'),
    _PhoneCountry(name: 'Hàn Quốc', isoCode: 'KR', dialCode: '+82'),
  ];

  bool useEmail = true;
  bool isSubmitting = false;
  _PhoneCountry selectedPhoneCountry = _phoneCountries.first;
  final TextEditingController emailController = TextEditingController();
  final AuthApiService _authApiService = AuthApiService();

  @override
  void dispose() {
    emailController.dispose();
    super.dispose();
  }

  Future<void> _showNotice({
    required String title,
    required String message,
  }) {
    return showDialog<void>(
      context: context,
      builder: (context) => AppNoticeDialog(
        title: title,
        message: message,
      ),
    );
  }

  Future<void> _handleForgotPassword() async {
    if (isSubmitting) {
      return;
    }

    if (!useEmail) {
      await _showNotice(
        title: 'Tạm thời chỉ hỗ trợ Email',
        message:
            'Hiện backend forgot password đang dùng email. Vui lòng chọn tab Email.',
      );
      return;
    }

    final email = emailController.text.trim();
    if (!Validators.isValidEmail(email)) {
      await _showNotice(
        title: 'Email chưa hợp lệ',
        message: 'Vui lòng nhập email đúng định dạng để nhận mã xác thực.',
      );
      return;
    }

    setState(() {
      isSubmitting = true;
    });

    String? errorMessage;
    try {
      await _authApiService.resendForgotPasswordOtp(email: email);
    } on AppException catch (exception) {
      errorMessage = exception.message;
    } catch (_) {
      errorMessage = 'Gửi yêu cầu chưa thành công. Vui lòng thử lại.';
    }

    if (!mounted) {
      return;
    }

    setState(() {
      isSubmitting = false;
    });

    if (errorMessage != null) {
      await _showNotice(
        title: 'Không thể gửi mã',
        message: errorMessage,
      );
      return;
    }

    Navigator.of(context).pushNamed(
      AppRouter.forgotPasswordOtp,
      arguments: email,
    );
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        toolbarHeight: 64,
        titleSpacing: 0,
        title: const Text(
          'Quên mật khẩu',
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
          padding: const EdgeInsets.fromLTRB(20, 20, 20, 28),
          child: Column(
            children: [
              const _RecoverySteps(),
              const SizedBox(height: 14),
              Container(
                width: 80,
                height: 80,
                decoration: const BoxDecoration(
                  color: AppColors.brandRed,
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.key_outlined,
                  size: 32,
                  color: AppColors.background,
                ),
              ),
              const SizedBox(height: 12),
              const Text(
                'Khôi phục tài khoản',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 26,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
              ),
              const SizedBox(height: 10),
              const Text(
                'Nhập email hoặc số điện thoại đã đăng ký để nhận mã xác thực khôi phục quyền truy cập.',
                textAlign: TextAlign.center,
                style: TextStyle(
                  fontSize: 16,
                  height: 1.45,
                  color: AppColors.textMuted,
                  fontWeight: FontWeight.w500,
                ),
              ),
              const SizedBox(height: 24),
              Container(
                padding: const EdgeInsets.all(4),
                decoration: BoxDecoration(
                  color: const Color(0xFFF2F4F7),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Row(
                  children: [
                    Expanded(
                      child: _ModeTab(
                        label: 'Email',
                        isActive: useEmail,
                        activeColor: AppColors.brandBlue,
                        onTap: () {
                          setState(() {
                            useEmail = true;
                          });
                        },
                      ),
                    ),
                    Expanded(
                      child: _ModeTab(
                        label: 'Số điện thoại',
                        isActive: !useEmail,
                        activeColor: AppColors.brandRed,
                        onTap: () {
                          setState(() {
                            useEmail = false;
                          });
                        },
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 22),
              Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  useEmail ? 'ĐỊA CHỈ EMAIL' : 'SỐ ĐIỆN THOẠI',
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: AppColors.textMuted,
                  ),
                ),
              ),
              const SizedBox(height: 8),
              useEmail
                  ? _RecoveryInput(
                      hintText: 'example@email.com',
                      prefixIcon: Icons.mail_outline,
                      controller: emailController,
                      keyboardType: TextInputType.emailAddress,
                    )
                  : _RecoveryPhoneInput(
                      selectedCountry: selectedPhoneCountry,
                      countries: _phoneCountries,
                      onCountryChanged: (country) {
                        setState(() {
                          selectedPhoneCountry = country;
                        });
                      },
                    ),
              const SizedBox(height: 20),
              SizedBox(
                width: double.infinity,
                height: 54,
                child: ElevatedButton(
                  onPressed: isSubmitting ? null : _handleForgotPassword,
                  child: isSubmitting
                      ? const SizedBox(
                          width: 22,
                          height: 22,
                          child: CircularProgressIndicator(
                            strokeWidth: 2.2,
                            color: AppColors.background,
                          ),
                        )
                      : const Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            Text('Gửi mã xác thực'),
                            SizedBox(width: 12),
                            Icon(Icons.arrow_forward_rounded),
                          ],
                        ),
                ),
              ),
              const SizedBox(height: 26),
              Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Text(
                    'Bạn đã nhớ mật khẩu? ',
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
              const SizedBox(height: 48),
              const Column(
                children: [
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      _BrandDot(),
                      SizedBox(width: 18),
                      _BrandDot(),
                      SizedBox(width: 18),
                      _BrandDot(),
                    ],
                  ),
                  SizedBox(height: 14),
                  Text.rich(
                    TextSpan(
                      children: [
                        TextSpan(
                          text: "EXOTIC'S ",
                          style: TextStyle(
                            color: Color(0xFFFF9D92),
                            letterSpacing: 2,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                        TextSpan(
                          text: 'SECURITY SYSTEM',
                          style: TextStyle(
                            color: Color(0xFF98A2B3),
                            letterSpacing: 2,
                            fontWeight: FontWeight.w600,
                          ),
                        ),
                      ],
                    ),
                    textAlign: TextAlign.center,
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

class _RecoverySteps extends StatelessWidget {
  const _RecoverySteps();

  @override
  Widget build(BuildContext context) {
    return const Row(
      children: [
        Expanded(
          child: _RecoveryStep(
            step: '1',
            label: 'YÊU CẦU',
            isActive: true,
          ),
        ),
        Expanded(child: _RecoveryDivider()),
        Expanded(
          child: _RecoveryStep(
            step: '2',
            label: 'XÁC THỰC',
          ),
        ),
        Expanded(child: _RecoveryDivider()),
        Expanded(
          child: _RecoveryStep(
            step: '3',
            label: 'CẤP LẠI',
          ),
        ),
      ],
    );
  }
}

class _RecoveryStep extends StatelessWidget {
  const _RecoveryStep({
    required this.step,
    required this.label,
    this.isActive = false,
  });

  final String step;
  final String label;
  final bool isActive;

  @override
  Widget build(BuildContext context) {
    return Column(
      children: [
        Container(
          width: 24,
          height: 24,
          decoration: BoxDecoration(
            color: isActive ? AppColors.brandBlue : const Color(0xFFF2F4F7),
            shape: BoxShape.circle,
            border: Border.all(
              color: isActive ? AppColors.brandBlue : AppColors.border,
            ),
          ),
          alignment: Alignment.center,
          child: Text(
            step,
            style: TextStyle(
              fontSize: 18,
              fontWeight: FontWeight.w700,
              color: isActive ? AppColors.background : AppColors.textMuted,
            ),
          ),
        ),
        const SizedBox(height: 10),
        Text(
          label,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w700,
            color: isActive ? AppColors.brandBlue : AppColors.textMuted,
            letterSpacing: 0.4,
          ),
        ),
      ],
    );
  }
}

class _RecoveryDivider extends StatelessWidget {
  const _RecoveryDivider();

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 1.5,
      margin: const EdgeInsets.only(bottom: 28),
      color: AppColors.border,
    );
  }
}

class _ModeTab extends StatelessWidget {
  const _ModeTab({
    required this.label,
    required this.isActive,
    required this.activeColor,
    required this.onTap,
  });

  final String label;
  final bool isActive;
  final Color activeColor;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 180),
        padding: const EdgeInsets.symmetric(vertical: 14),
        decoration: BoxDecoration(
          color: isActive ? AppColors.background : Colors.transparent,
          borderRadius: BorderRadius.circular(14),
          boxShadow: isActive
              ? const [
                  BoxShadow(
                    color: AppColors.shadow,
                    blurRadius: 8,
                    offset: Offset(0, 3),
                  ),
                ]
              : null,
        ),
        child: Text(
          label,
          textAlign: TextAlign.center,
          style: TextStyle(
            fontSize: 15,
            fontWeight: FontWeight.w700,
            color: isActive ? activeColor : AppColors.textMuted,
          ),
        ),
      ),
    );
  }
}

class _RecoveryInput extends StatelessWidget {
  const _RecoveryInput({
    required this.hintText,
    required this.prefixIcon,
    this.controller,
    this.keyboardType,
  });

  final String hintText;
  final IconData prefixIcon;
  final TextEditingController? controller;
  final TextInputType? keyboardType;

  @override
  Widget build(BuildContext context) {
    return TextField(
      controller: controller,
      keyboardType: keyboardType,
      decoration: InputDecoration(
        hintText: hintText,
        prefixIcon: Icon(
          prefixIcon,
          color: AppColors.brandBlue,
        ),
      ),
    );
  }
}

class _RecoveryPhoneInput extends StatelessWidget {
  const _RecoveryPhoneInput({
    required this.selectedCountry,
    required this.countries,
    required this.onCountryChanged,
  });

  final _PhoneCountry selectedCountry;
  final List<_PhoneCountry> countries;
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
                              color: AppColors.brandBlue,
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
                      color: AppColors.brandBlue,
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

class _BrandDot extends StatelessWidget {
  const _BrandDot();

  @override
  Widget build(BuildContext context) {
    return Container(
      width: 10,
      height: 10,
      decoration: const BoxDecoration(
        color: Color(0xFF98BEE0),
        shape: BoxShape.circle,
      ),
    );
  }
}
