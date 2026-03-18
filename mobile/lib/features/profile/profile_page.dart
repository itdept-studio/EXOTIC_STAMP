import 'package:flutter/material.dart';

import '../../core/widgets/placeholder_scaffold.dart';

class ProfilePage extends StatelessWidget {
  const ProfilePage({super.key});

  @override
  Widget build(BuildContext context) {
    return const PlaceholderScaffold(
      title: 'Profile',
      description: 'Thong tin user, setting va trang thai tai khoan.',
    );
  }
}

