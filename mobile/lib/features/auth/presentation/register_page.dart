import 'package:flutter/material.dart';
import 'package:font_awesome_flutter/font_awesome_flutter.dart';

import '../../../app/theme/app_colors.dart';
import '../../../app/router.dart';

class RegisterPage extends StatefulWidget {
  const RegisterPage({super.key});

  @override
  State<RegisterPage> createState() => _RegisterPageState();
}

class _RegisterPageState extends State<RegisterPage> {
  bool agreeToTerms = false;
  bool obscurePassword = true;
  bool obscureConfirmPassword = true;

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
              const _RegisterInputField(
                hintText: 'Nguyễn Văn A',
                prefixIcon: Icons.person_outline,
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(text: 'Email'),
              const SizedBox(height: 8),
              const _RegisterInputField(
                hintText: 'example@gmail.com',
                prefixIcon: Icons.mail_outline,
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(
                text: 'Số điện thoại',
                trailingText: '(Tùy chọn)',
              ),
              const SizedBox(height: 8),
              const _RegisterInputField(
                hintText: '+84 901 234 567',
                prefixIcon: Icons.phone_outlined,
              ),
              const SizedBox(height: 18),
              const _RegisterLabel(text: 'Mật khẩu'),
              const SizedBox(height: 8),
              _RegisterInputField(
                hintText: '........',
                prefixIcon: Icons.lock_outline,
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
                  onPressed: () {},
                  child: const Text('Đăng Ký Ngay'),
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

class _RegisterInputField extends StatelessWidget {
  const _RegisterInputField({
    required this.hintText,
    required this.prefixIcon,
    this.suffixIcon,
    this.obscureText = false,
  });

  final String hintText;
  final IconData prefixIcon;
  final Widget? suffixIcon;
  final bool obscureText;

  @override
  Widget build(BuildContext context) {
    return TextField(
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
