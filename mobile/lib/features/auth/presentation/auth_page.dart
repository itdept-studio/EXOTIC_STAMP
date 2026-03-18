import 'package:flutter/material.dart';

import '../../../core/widgets/placeholder_scaffold.dart';

class AuthPage extends StatelessWidget {
  const AuthPage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderScaffold(
      title: 'Auth',
      description: 'Dang ky, dang nhap va luu phien nguoi dung.',
    );
  }
}

