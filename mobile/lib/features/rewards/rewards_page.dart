import 'package:flutter/material.dart';

import '../../app/router.dart';
import '../../app/theme/app_colors.dart';

class RewardsPage extends StatelessWidget {
  const RewardsPage({super.key});

  static const int _currentStamps = 5;
  static const int _goalStamps = 14;

  static const List<_RewardMilestone> _milestones = [
    _RewardMilestone(
      stampCount: 0,
      title: 'Metro Cadet',
      description: 'Silver Digital Sticker Pack',
      icon: Icons.star_border_rounded,
      state: _RewardState.claimed,
    ),
    _RewardMilestone(
      stampCount: 7,
      title: 'Frequent Rider',
      description: 'Free Espresso at Partner Cafe',
      icon: Icons.coffee_outlined,
      state: _RewardState.claimable,
    ),
    _RewardMilestone(
      stampCount: 14,
      title: 'Metro Legend',
      description: 'Exclusive Gold Pin & 50% Brand Voucher',
      icon: Icons.emoji_events_outlined,
      state: _RewardState.locked,
    ),
  ];

  static const List<_VoucherReward> _vouchers = [
    _VoucherReward(
      brand: 'Metro BrewStop',
      offer: 'Buy 1 Get 1 Coffee',
      expiryText: 'Expires Dec 24, 2024',
      accent: AppColors.brandRed,
      icon: Icons.local_cafe_outlined,
      imageUrl:
          'https://i.pinimg.com/736x/2b/f6/bd/2bf6bdaba3e49720bea75a55d05dba3b.jpg',
    ),
    _VoucherReward(
      brand: 'Station Snacks',
      offer: '20% Off All Pastries',
      expiryText: 'Expires Dec 30, 2024',
      accent: AppColors.brandBlue,
      icon: Icons.bakery_dining_outlined,
    ),
    _VoucherReward(
      brand: 'TechHub Metro',
      offer: '\$10 Gift Voucher',
      expiryText: 'Expires Jan 15, 2025',
      accent: AppColors.brandBlue,
      icon: Icons.storefront_outlined,
    ),
  ];

  @override
  Widget build(BuildContext context) {
    const progress = _currentStamps / _goalStamps;

    return Scaffold(
      backgroundColor: const Color(0xFFFDFDFE),
      floatingActionButtonLocation: FloatingActionButtonLocation.centerDocked,
      floatingActionButton: Transform.translate(
        offset: const Offset(0, 12),
        child: Container(
          width: 78,
          height: 78,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            color: AppColors.background,
            boxShadow: [
              BoxShadow(
                color: AppColors.brandRed.withValues(alpha: 0.24),
                blurRadius: 22,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Padding(
            padding: const EdgeInsets.all(6),
            child: FloatingActionButton(
              elevation: 0,
              backgroundColor: AppColors.brandRed,
              foregroundColor: AppColors.background,
              onPressed: () => Navigator.of(context).pushNamed(AppRouter.scan),
              shape: const CircleBorder(),
              child: const Icon(Icons.center_focus_strong_rounded, size: 30),
            ),
          ),
        ),
      ),
      bottomNavigationBar: _RewardsBottomBar(
        selectedIndex: 1,
        onSelected: (index) {
          switch (index) {
            case 0:
              Navigator.of(context).pushReplacementNamed(AppRouter.home);
              break;
            case 1:
              break;
            case 2:
              Navigator.of(context).pushReplacementNamed(AppRouter.stations);
              break;
            case 3:
              Navigator.of(context).pushReplacementNamed(AppRouter.profile);
              break;
          }
        },
      ),
      body: SafeArea(
        bottom: false,
        child: CustomScrollView(
          slivers: [
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 22, 20, 18),
                child: Row(
                  children: [
                    Container(
                      width: 42,
                      height: 42,
                      decoration: BoxDecoration(
                        color: AppColors.brandBlue,
                        borderRadius: BorderRadius.circular(24),
                      ),
                      child: const Icon(
                        Icons.card_giftcard_rounded,
                        color: AppColors.background,
                        size: 22,
                      ),
                    ),
                    const SizedBox(width: 18),
                    const Expanded(
                      child: Text(
                        'Rewards',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w900,
                          color: AppColors.textPrimary,
                        ),
                      ),
                    ),
                    IconButton(
                      onPressed: () {},
                      icon: const Icon(
                        Icons.redeem_outlined,
                        size: 28,
                        color: AppColors.brandBlue,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            const SliverToBoxAdapter(
              child: Padding(
                padding: EdgeInsets.fromLTRB(20, 0, 20, 22),
                child: _RewardsProgressCard(progress: progress),
              ),
            ),
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 8, 20, 10),
                child: Row(
                  children: [
                    const Expanded(
                      child: Text(
                        'Road to 14',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w900,
                          color: AppColors.textPrimary,
                        ),
                      ),
                    ),
                    TextButton(
                      onPressed: () {},
                      child: const Text(
                        'View Milestones',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                          color: AppColors.brandBlue,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
            ),
            SliverPadding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 10),
              sliver: SliverList.separated(
                itemCount: _milestones.length,
                itemBuilder: (context, index) {
                  return _MilestoneCard(
                    milestone: _milestones[index],
                    isFirst: index == 0,
                    isLast: index == _milestones.length - 1,
                  );
                },
                separatorBuilder: (_, __) => const SizedBox(height: 20),
              ),
            ),
            SliverToBoxAdapter(
              child: Padding(
                padding: const EdgeInsets.fromLTRB(20, 20, 20, 10),
                child: Row(
                  children: [
                    const Expanded(
                      child: Text(
                        'Available Vouchers',
                        style: TextStyle(
                          fontSize: 20,
                          fontWeight: FontWeight.w900,
                          color: AppColors.textPrimary,
                        ),
                      ),
                    ),
                    TextButton.icon(
                      onPressed: () {},
                      iconAlignment: IconAlignment.end,
                      icon: const Icon(
                        Icons.chevron_right_rounded,
                        size: 18,
                      ),
                      label: const Text(
                        'History',
                        style: TextStyle(
                          fontSize: 15,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      style: TextButton.styleFrom(
                        foregroundColor: AppColors.brandBlue,
                      ),
                    ),
                  ],
                ),
              ),
            ),
            SliverPadding(
              padding: const EdgeInsets.fromLTRB(20, 0, 20, 120),
              sliver: SliverList.separated(
                itemCount: _vouchers.length,
                itemBuilder: (context, index) {
                  return _VoucherCard(voucher: _vouchers[index]);
                },
                separatorBuilder: (_, __) => const SizedBox(height: 20),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _RewardsProgressCard extends StatelessWidget {
  const _RewardsProgressCard({required this.progress});

  final double progress;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: const Color(0xFFF0F7FF),
        borderRadius: BorderRadius.circular(28),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 22,
            offset: Offset(0, 10),
          ),
        ],
      ),
      child: Stack(
        children: [
          Positioned(
            top: -8,
            right: -6,
            child: Icon(
              Icons.emoji_events_outlined,
              size: 170,
              color: AppColors.brandBlue.withValues(alpha: 0.08),
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text(
                'YOUR PROGRESS',
                style: TextStyle(
                  fontSize: 15,
                  fontWeight: FontWeight.w900,
                  letterSpacing: 1.2,
                  color: AppColors.brandBlue,
                ),
              ),
              const SizedBox(height: 12),
              RichText(
                text: const TextSpan(
                  children: [
                    TextSpan(
                      text: '5',
                      style: TextStyle(
                        fontSize: 30,
                        height: 0.9,
                        fontWeight: FontWeight.w900,
                        color: AppColors.textPrimary,
                      ),
                    ),
                    TextSpan(
                      text: ' / 14 Stamps',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w500,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 22),
              ClipRRect(
                borderRadius: BorderRadius.circular(999),
                child: LinearProgressIndicator(
                  value: progress,
                  minHeight: 18,
                  backgroundColor: const Color(0xFFD5E7FA),
                  valueColor:
                      const AlwaysStoppedAnimation<Color>(AppColors.brandBlue),
                ),
              ),
              const SizedBox(height: 14),
              const Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  _ProgressMark(label: 'START'),
                  _ProgressMark(label: '3'),
                  _ProgressMark(label: '7'),
                  _ProgressMark(label: '14 (GOAL)', isGoal: true),
                ],
              ),
              const SizedBox(height: 26),
              const Wrap(
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 2,
                runSpacing: 2,
                children: [
                  Icon(
                    Icons.bolt_rounded,
                    color: AppColors.brandRed,
                    size: 26,
                  ),
                  Text(
                    'Next reward: ',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: AppColors.textPrimary,
                    ),
                  ),
                  Text(
                    'Free Coffee',
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w900,
                      color: AppColors.brandBlue,
                    ),
                  ),
                  Text(
                    'in 2 stamps!',
                    style: TextStyle(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: AppColors.textPrimary,
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _ProgressMark extends StatelessWidget {
  const _ProgressMark({required this.label, this.isGoal = false});

  final String label;
  final bool isGoal;

  @override
  Widget build(BuildContext context) {
    return Text(
      label,
      style: TextStyle(
        fontSize: 14,
        fontWeight: FontWeight.w900,
        color: isGoal ? const Color(0xFFF28B82) : const Color(0xFF7A8494),
      ),
    );
  }
}

class _MilestoneCard extends StatelessWidget {
  const _MilestoneCard({
    required this.milestone,
    required this.isFirst,
    required this.isLast,
  });

  final _RewardMilestone milestone;
  final bool isFirst;
  final bool isLast;

  @override
  Widget build(BuildContext context) {
    final isClaimed = milestone.state == _RewardState.claimed;
    final isClaimable = milestone.state == _RewardState.claimable;
    final isLocked = milestone.state == _RewardState.locked;

    return SizedBox(
      height: 150,
      child: Row(
        children: [
          SizedBox(
            width: 76,
            child: Column(
              children: [
                Expanded(
                  child: Container(
                    width: 4,
                    color:
                        isFirst ? Colors.transparent : const Color(0xFFE3E7EE),
                  ),
                ),
                Container(
                  width: 36,
                  height: 36,
                  decoration: BoxDecoration(
                    shape: BoxShape.circle,
                    color:
                        isClaimed ? AppColors.brandBlue : AppColors.background,
                    border: Border.all(
                      color: isLocked
                          ? const Color(0xFFE5E8EE)
                          : AppColors.brandBlue,
                      width: 5,
                    ),
                  ),
                  alignment: Alignment.center,
                  child: isClaimed
                      ? const Icon(
                          Icons.check_rounded,
                          color: AppColors.background,
                          size: 18,
                        )
                      : Text(
                          '${milestone.stampCount}',
                          style: TextStyle(
                            fontSize: 18,
                            fontWeight: FontWeight.w900,
                            color: isLocked
                                ? const Color(0xFFABB2C0)
                                : AppColors.brandRed,
                          ),
                        ),
                ),
                Expanded(
                  child: Container(
                    width: 4,
                    color:
                        isLast ? Colors.transparent : const Color(0xFFE3E7EE),
                  ),
                ),
              ],
            ),
          ),
          const SizedBox(width: 2),
          Expanded(
            child: Container(
              padding: const EdgeInsets.all(18),
              decoration: BoxDecoration(
                color:
                    isLocked ? const Color(0xFFF5F6F9) : AppColors.background,
                borderRadius: BorderRadius.circular(28),
                boxShadow: const [
                  BoxShadow(
                    color: AppColors.shadow,
                    blurRadius: 18,
                    offset: Offset(0, 8),
                  ),
                ],
              ),
              child: Row(
                children: [
                  Container(
                    width: 42,
                    height: 42,
                    decoration: BoxDecoration(
                      color: const Color(0xFFEFF6FF),
                      borderRadius: BorderRadius.circular(14),
                    ),
                    child: Icon(
                      milestone.icon,
                      color: isLocked
                          ? const Color(0xFF7D8490)
                          : AppColors.brandBlue,
                      size: 22,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Text(
                          milestone.title,
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            fontSize: 12,
                            fontWeight: FontWeight.w900,
                            color: isLocked
                                ? const Color(0xFF5D6572)
                                : AppColors.textPrimary,
                          ),
                        ),
                        const SizedBox(height: 6),
                        Text(
                          milestone.description,
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                          style: TextStyle(
                            fontSize: 12,
                            height: 1.25,
                            color: isLocked
                                ? const Color(0xFF808896)
                                : AppColors.textPrimary,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(width: 12),
                  if (isClaimed)
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 10,
                        vertical: 10,
                      ),
                      decoration: BoxDecoration(
                        borderRadius: BorderRadius.circular(999),
                        border: Border.all(
                            color: const Color(0xFFD7DCE4), width: 2),
                      ),
                      child: const Text(
                        'Claimed',
                        style: TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                    )
                  else if (isClaimable)
                    FilledButton(
                      onPressed: () {},
                      style: FilledButton.styleFrom(
                        backgroundColor: AppColors.brandBlue,
                        foregroundColor: AppColors.background,
                        padding: const EdgeInsets.symmetric(
                          horizontal: 15,
                          vertical: 10,
                        ),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(24),
                        ),
                        textStyle: const TextStyle(
                          fontSize: 12,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      child: const Text('Claim'),
                    )
                  else
                    const Icon(
                      Icons.lock_outline_rounded,
                      color: Color(0xFF848B97),
                      size: 28,
                    ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }
}

class _VoucherCard extends StatelessWidget {
  const _VoucherCard({required this.voucher});

  final _VoucherReward voucher;

  @override
  Widget build(BuildContext context) {
    return Container(
      decoration: BoxDecoration(
        color: AppColors.background,
        borderRadius: BorderRadius.circular(30),
        boxShadow: const [
          BoxShadow(
            color: AppColors.shadow,
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(18, 18, 18, 16),
            child: Row(
              children: [
                Container(
                  width: 42,
                  height: 42,
                  decoration: const BoxDecoration(
                    color: Color(0xFFEFF6FF),
                    shape: BoxShape.circle,
                  ),
                  clipBehavior: Clip.antiAlias,
                  child: voucher.imageUrl != null
                      ? Image.network(
                          voucher.imageUrl!,
                          fit: BoxFit.cover,
                          errorBuilder: (_, __, ___) => Icon(
                            voucher.icon,
                            size: 24,
                            color: voucher.accent,
                          ),
                        )
                      : Icon(
                          voucher.icon,
                          size: 24,
                          color: voucher.accent,
                        ),
                ),
                const SizedBox(width: 16),
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        voucher.brand,
                        style: const TextStyle(
                          fontSize: 14,
                          fontWeight: FontWeight.w900,
                          color: AppColors.textPrimary,
                        ),
                      ),
                      const SizedBox(height: 6),
                      Row(
                        children: [
                          const Icon(
                            Icons.access_time_rounded,
                            size: 14,
                            color: AppColors.textPrimary,
                          ),
                          const SizedBox(width: 6),
                          Expanded(
                            child: Text(
                              voucher.expiryText,
                              style: const TextStyle(
                                fontSize: 14,
                                color: AppColors.textPrimary,
                              ),
                            ),
                          ),
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 10),
                Icon(
                  Icons.star_border_rounded,
                  size: 24,
                  color: voucher.accent,
                ),
              ],
            ),
          ),
          Container(
            width: double.infinity,
            padding: const EdgeInsets.fromLTRB(18, 22, 18, 22),
            decoration: BoxDecoration(
              color: const Color(0xFFEFF6FF),
              borderRadius: const BorderRadius.vertical(
                bottom: Radius.circular(30),
              ),
              border: Border(
                top: BorderSide(
                  color: const Color(0xFFB7D8FA).withValues(alpha: 0.9),
                  width: 2,
                ),
              ),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Text(
                    voucher.offer,
                    style: TextStyle(
                      fontSize: 16,
                      fontWeight: FontWeight.w900,
                      color: voucher.accent,
                    ),
                  ),
                ),
                const SizedBox(width: 16),
                OutlinedButton(
                  onPressed: () {},
                  style: OutlinedButton.styleFrom(
                    foregroundColor: voucher.accent,
                    side: BorderSide(color: voucher.accent, width: 1.5),
                    padding: const EdgeInsets.symmetric(
                      horizontal: 14,
                      vertical: 12,
                    ),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(22),
                    ),
                    textStyle: const TextStyle(
                      fontSize: 14,
                      fontWeight: FontWeight.w900,
                    ),
                  ),
                  child: const Text('Redeem'),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _RewardsBottomBar extends StatelessWidget {
  const _RewardsBottomBar({
    required this.selectedIndex,
    required this.onSelected,
  });

  final int selectedIndex;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return BottomAppBar(
      height: 78,
      padding: const EdgeInsets.symmetric(horizontal: 8),
      color: AppColors.background,
      surfaceTintColor: AppColors.background,
      child: Row(
        children: [
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _RewardsNavItem(
                  icon: Icons.home_outlined,
                  label: 'Home',
                  isActive: selectedIndex == 0,
                  onTap: () => onSelected(0),
                ),
                _RewardsNavItem(
                  icon: Icons.book_outlined,
                  label: 'Book',
                  isActive: selectedIndex == 1,
                  onTap: () => onSelected(1),
                ),
              ],
            ),
          ),
          const SizedBox(width: 56),
          Expanded(
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _RewardsNavItem(
                  icon: Icons.format_list_bulleted_rounded,
                  label: 'Stations',
                  isActive: selectedIndex == 2,
                  onTap: () => onSelected(2),
                ),
                _RewardsNavItem(
                  icon: Icons.person_outline_rounded,
                  label: 'Profile',
                  isActive: selectedIndex == 3,
                  onTap: () => onSelected(3),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _RewardsNavItem extends StatelessWidget {
  const _RewardsNavItem({
    required this.icon,
    required this.label,
    required this.isActive,
    required this.onTap,
  });

  final IconData icon;
  final String label;
  final bool isActive;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(14),
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 6),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            Icon(
              icon,
              color: isActive ? AppColors.brandBlue : AppColors.textPrimary,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: TextStyle(
                fontSize: 11,
                fontWeight: FontWeight.w700,
                color: isActive ? AppColors.brandBlue : AppColors.textPrimary,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

enum _RewardState { claimed, claimable, locked }

class _RewardMilestone {
  const _RewardMilestone({
    required this.stampCount,
    required this.title,
    required this.description,
    required this.icon,
    required this.state,
  });

  final int stampCount;
  final String title;
  final String description;
  final IconData icon;
  final _RewardState state;
}

class _VoucherReward {
  const _VoucherReward({
    required this.brand,
    required this.offer,
    required this.expiryText,
    required this.accent,
    required this.icon,
    this.imageUrl,
  });

  final String brand;
  final String offer;
  final String expiryText;
  final Color accent;
  final IconData icon;
  final String? imageUrl;
}
