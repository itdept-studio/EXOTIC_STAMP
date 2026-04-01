import 'package:flutter/material.dart';

import '../../app/theme/app_colors.dart';

class StampBookPage extends StatefulWidget {
  const StampBookPage({super.key});

  @override
  State<StampBookPage> createState() => _StampBookPageState();
}

class _StampBookPageState extends State<StampBookPage> {
  int selectedFilterIndex = 0;

  static const List<_StampFilter> _filters = [
    _StampFilter(label: 'All Lines'),
    _StampFilter(label: 'Line 1'),
    _StampFilter(label: 'Line 2'),
  ];

  static const List<_StampBookItem> _items = [
    _StampBookItem(
      title: 'CENTRAL MALL',
      imagePath:
          'assets/stamps/z7633669581431_66e47f73a613de962410fc4e3ce82538.jpg',
      isCollected: true,
      line: 'Line 1',
    ),
    _StampBookItem(
      title: 'GRAND PARK',
      imagePath:
          'assets/stamps/z7633669581471_d24496e11ae33e6b864c38edb12d262d.jpg',
      isCollected: true,
      line: 'Line 1',
    ),
    _StampBookItem(
      title: 'AIRPORT EAST',
      imagePath:
          'assets/stamps/z7633669606889_3dd34dbfa8618b2c9b12038b95575238.jpg',
      isCollected: true,
      line: 'Line 2',
    ),
    _StampBookItem(
      title: 'SUNSET BLVD',
      imagePath:
          'assets/stamps/z7633669757228_5888c62a474a08d929931db5018dbf36.jpg',
      isCollected: false,
      line: 'Line 2',
    ),
    _StampBookItem(
      title: 'TECH HUB',
      imagePath:
          'assets/stamps/z7633669834885_7c89fa05dc6bf219e40460efaf9ff5e4.jpg',
      isCollected: false,
      line: 'Line 2',
    ),
    _StampBookItem(
      title: 'HARBOR VIEW',
      imagePath:
          'assets/stamps/z7633669880666_b50acc4cb89073afbe11f73d1930fc9c.jpg',
      isCollected: true,
      line: 'Line 1',
    ),
    _StampBookItem(
      title: 'OLD QUARTER',
      imagePath:
          'assets/stamps/z7633669972596_414f583f462e8904a2b7021545cc05ba.jpg',
      isCollected: false,
      line: 'Line 1',
    ),
    _StampBookItem(
      title: 'CITY SQUARE',
      imagePath:
          'assets/stamps/z7633670018993_c4527a120b3265f646643f046dd46355.jpg',
      isCollected: true,
      line: 'Line 1',
    ),
    _StampBookItem(
      title: 'NORTH BRIDGE',
      imagePath:
          'assets/stamps/z7633670089957_a017d808dbeda953fe47ea38629b5c4b.jpg',
      isCollected: false,
      line: 'Line 2',
    ),
    _StampBookItem(
      title: 'RIVER WALK',
      imagePath:
          'assets/stamps/z7633670219952_d91823be36bbb04ef40f2b54ed00a920.jpg',
      isCollected: false,
      line: 'Line 1',
    ),
    _StampBookItem(
      title: 'SKY TOWER',
      imagePath:
          'assets/stamps/z7633670375782_05f0fec51330ed57474548949cc8c2c1.jpg',
      isCollected: false,
      line: 'Line 2',
    ),
    _StampBookItem(
      title: 'GREEN VALLEY',
      imagePath:
          'assets/stamps/z7633670486944_5ef50a8bed4afc1200c13b08e61daca6.jpg',
      isCollected: false,
      line: 'Line 2',
    ),
    _StampBookItem(
      title: 'OPERA HOUSE',
      imagePath:
          'assets/stamps/z7633670582197_7cd23d1bacb7e4e1573692071103bb13.jpg',
      isCollected: false,
      line: 'Line 1',
    ),
    _StampBookItem(
      title: 'UNIVERSITY',
      imagePath:
          'assets/stamps/z7633670613254_8e32bf81aa5b4a3da9142f6dd7a2126f.jpg',
      isCollected: false,
      line: 'Line 1',
    ),
  ];

  List<_StampBookItem> get _visibleItems {
    final selected = _filters[selectedFilterIndex].label;
    if (selected == 'All Lines') {
      return _items;
    }
    return _items.where((item) => item.line == selected).toList();
  }

  @override
  Widget build(BuildContext context) {
    final collectedCount = _items.where((item) => item.isCollected).length;
    final totalCount = _items.length;
    final progress = collectedCount / totalCount;

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        titleSpacing: 16,
        title: const Text.rich(
          TextSpan(
            children: [
              TextSpan(
                text: 'Stamp ',
                style: TextStyle(
                  color: AppColors.brandRed,
                  fontSize: 18,
                  fontWeight: FontWeight.w800,
                ),
              ),
              TextSpan(
                text: 'Book',
                style: TextStyle(
                  color: AppColors.brandBlue,
                  fontSize: 18,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ],
          ),
        ),
        actions: [
          IconButton(
            onPressed: () {},
            icon: const Icon(Icons.search_rounded),
          ),
        ],
        bottom: PreferredSize(
          preferredSize: const Size.fromHeight(1),
          child: Container(
            height: 1,
            color: const Color(0xFFE8ECF3),
          ),
        ),
      ),
      body: SafeArea(
        top: false,
        child: SingleChildScrollView(
          padding: const EdgeInsets.fromLTRB(14, 16, 14, 28),
          child: Column(
            children: [
              _StampSummaryCard(
                progress: progress,
                collectedCount: collectedCount,
                totalCount: totalCount,
              ),
              const SizedBox(height: 18),
              _StampFilterBar(
                filters: _filters,
                selectedIndex: selectedFilterIndex,
                onSelected: (index) {
                  setState(() {
                    selectedFilterIndex = index;
                  });
                },
              ),
              const SizedBox(height: 18),
              GridView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: _visibleItems.length,
                gridDelegate: const SliverGridDelegateWithFixedCrossAxisCount(
                  crossAxisCount: 3,
                  crossAxisSpacing: 12,
                  mainAxisSpacing: 18,
                  childAspectRatio: 0.72,
                ),
                itemBuilder: (context, index) {
                  return _StampCard(item: _visibleItems[index]);
                },
              ),
              const SizedBox(height: 28),
              const Padding(
                padding: EdgeInsets.symmetric(horizontal: 16),
                child: Text.rich(
                  TextSpan(
                    style: TextStyle(
                      fontSize: 15,
                      height: 1.6,
                      fontWeight: FontWeight.w500,
                      color: AppColors.textMuted,
                    ),
                    children: [
                      TextSpan(
                        text: 'Visit stations and look for the ',
                      ),
                      TextSpan(
                        text: 'NFC tags',
                        style: TextStyle(
                          color: AppColors.brandBlue,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      TextSpan(
                        text: ' or ',
                      ),
                      TextSpan(
                        text: 'QR codes',
                        style: TextStyle(
                          color: AppColors.brandRed,
                          fontWeight: FontWeight.w800,
                        ),
                      ),
                      TextSpan(
                        text: ' to collect your missing stamps!',
                      ),
                    ],
                  ),
                  textAlign: TextAlign.center,
                ),
              ),
              const SizedBox(height: 14),
              Row(
                children: [
                  const Expanded(
                    child: Divider(color: Color(0xFFE6EAF0), thickness: 1),
                  ),
                  Container(
                    margin: const EdgeInsets.symmetric(horizontal: 14),
                    padding: const EdgeInsets.symmetric(
                      horizontal: 18,
                      vertical: 6,
                    ),
                    decoration: BoxDecoration(
                      color: const Color(0xFFF4F6FA),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: const Text(
                      'Metro Stamp Collector v2.4',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textPrimary,
                      ),
                    ),
                  ),
                  const Expanded(
                    child: Divider(color: Color(0xFFE6EAF0), thickness: 1),
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

class _StampSummaryCard extends StatelessWidget {
  const _StampSummaryCard({
    required this.progress,
    required this.collectedCount,
    required this.totalCount,
  });

  final double progress;
  final int collectedCount;
  final int totalCount;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.fromLTRB(22, 22, 22, 22),
      decoration: BoxDecoration(
        color: const Color(0xFFF2F8FF),
        borderRadius: BorderRadius.circular(22),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Collection Status',
                      style: TextStyle(
                        fontSize: 14,
                        fontWeight: FontWeight.w700,
                        color: AppColors.textMuted,
                      ),
                    ),
                    SizedBox(height: 2),
                    Text(
                      'System Wide',
                      style: TextStyle(
                        fontSize: 22,
                        fontWeight: FontWeight.w900,
                        color: AppColors.brandBlue,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                width: 58,
                height: 58,
                decoration: const BoxDecoration(
                  color: Color(0xFFDFF0FF),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.emoji_events_outlined,
                  color: AppColors.brandRed,
                  size: 28,
                ),
              ),
            ],
          ),
          const SizedBox(height: 18),
          Row(
            children: [
              Container(
                padding:
                    const EdgeInsets.symmetric(horizontal: 12, vertical: 7),
                decoration: BoxDecoration(
                  color: AppColors.brandBlue,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Text(
                  '${(progress * 100).round()}% COMPLETED',
                  style: const TextStyle(
                    fontSize: 13,
                    fontWeight: FontWeight.w800,
                    color: AppColors.background,
                  ),
                ),
              ),
              const Spacer(),
              Text(
                '$collectedCount / $totalCount Stamps',
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w800,
                  color: AppColors.textPrimary,
                ),
              ),
            ],
          ),
          const SizedBox(height: 14),
          ClipRRect(
            borderRadius: BorderRadius.circular(999),
            child: LinearProgressIndicator(
              value: progress,
              minHeight: 10,
              backgroundColor: const Color(0xFFD9E3EF),
              valueColor: const AlwaysStoppedAnimation(AppColors.brandBlue),
            ),
          ),
        ],
      ),
    );
  }
}

class _StampFilterBar extends StatelessWidget {
  const _StampFilterBar({
    required this.filters,
    required this.selectedIndex,
    required this.onSelected,
  });

  final List<_StampFilter> filters;
  final int selectedIndex;
  final ValueChanged<int> onSelected;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(
        color: const Color(0xFFF2F4F7),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Row(
        children: [
          for (int index = 0; index < filters.length; index++)
            Expanded(
              child: GestureDetector(
                onTap: () => onSelected(index),
                child: AnimatedContainer(
                  duration: const Duration(milliseconds: 180),
                  padding: const EdgeInsets.symmetric(vertical: 12),
                  decoration: BoxDecoration(
                    color: selectedIndex == index
                        ? AppColors.background
                        : Colors.transparent,
                    borderRadius: BorderRadius.circular(14),
                    boxShadow: selectedIndex == index
                        ? const [
                            BoxShadow(
                              color: Color(0x0D000000),
                              blurRadius: 10,
                              offset: Offset(0, 4),
                            ),
                          ]
                        : null,
                  ),
                  child: Text(
                    filters[index].label,
                    textAlign: TextAlign.center,
                    style: TextStyle(
                      fontSize: 15,
                      fontWeight: FontWeight.w800,
                      color: selectedIndex == index
                          ? AppColors.brandBlue
                          : AppColors.brandBlue,
                    ),
                  ),
                ),
              ),
            ),
        ],
      ),
    );
  }
}

class _StampCard extends StatelessWidget {
  const _StampCard({required this.item});

  final _StampBookItem item;

  @override
  Widget build(BuildContext context) {
    final borderColor =
        item.isCollected ? AppColors.brandBlue : const Color(0xFFD7DEE8);
    final overlayColor = item.isCollected
        ? Colors.transparent
        : Colors.white.withValues(alpha: 0.35);

    return Column(
      children: [
        Expanded(
          child: Container(
            padding: const EdgeInsets.all(6),
            decoration: BoxDecoration(
              color: AppColors.background,
              borderRadius: BorderRadius.circular(18),
              border: Border.all(color: borderColor, width: 2.4),
            ),
            child: Stack(
              children: [
                Container(
                  clipBehavior: Clip.antiAlias,
                  decoration: BoxDecoration(
                    borderRadius: BorderRadius.circular(14),
                  ),
                  child: item.isCollected
                      ? Image.asset(
                          item.imagePath,
                          fit: BoxFit.cover,
                        )
                      : ColorFiltered(
                          colorFilter: const ColorFilter.matrix(<double>[
                            0.2126,
                            0.7152,
                            0.0722,
                            0,
                            0,
                            0.2126,
                            0.7152,
                            0.0722,
                            0,
                            0,
                            0.2126,
                            0.7152,
                            0.0722,
                            0,
                            0,
                            0,
                            0,
                            0,
                            1,
                            0,
                          ]),
                          child: Image.asset(
                            item.imagePath,
                            fit: BoxFit.cover,
                          ),
                        ),
                ),
                Positioned.fill(
                  child: DecoratedBox(
                    decoration: BoxDecoration(
                      color: overlayColor,
                      borderRadius: BorderRadius.circular(14),
                    ),
                  ),
                ),
                Positioned(
                  top: 8,
                  right: 8,
                  child: Container(
                    width: 26,
                    height: 26,
                    decoration: BoxDecoration(
                      color: item.isCollected
                          ? AppColors.background
                          : AppColors.brandBlue.withValues(alpha: 0.12),
                      shape: BoxShape.circle,
                      border: Border.all(
                        color: AppColors.brandBlue,
                        width: 2,
                      ),
                    ),
                    child: Icon(
                      item.isCollected
                          ? Icons.check_rounded
                          : Icons.lock_outline_rounded,
                      size: 16,
                      color: item.isCollected
                          ? AppColors.brandBlue
                          : AppColors.brandRed,
                    ),
                  ),
                ),
                if (!item.isCollected)
                  const Center(
                    child: Icon(
                      Icons.lock_outline_rounded,
                      size: 32,
                      color: AppColors.brandRed,
                    ),
                  ),
                Positioned(
                  left: 0,
                  right: 0,
                  bottom: 0,
                  child: Container(
                    height: 6,
                    decoration: BoxDecoration(
                      color: item.isCollected
                          ? AppColors.brandBlue
                          : AppColors.brandRed,
                      borderRadius: const BorderRadius.vertical(
                        bottom: Radius.circular(12),
                      ),
                    ),
                  ),
                ),
              ],
            ),
          ),
        ),
        const SizedBox(height: 8),
        Text(
          item.title,
          textAlign: TextAlign.center,
          style: const TextStyle(
            fontSize: 12,
            fontWeight: FontWeight.w800,
            color: AppColors.textPrimary,
          ),
        ),
      ],
    );
  }
}

class _StampFilter {
  const _StampFilter({required this.label});

  final String label;
}

class _StampBookItem {
  const _StampBookItem({
    required this.title,
    required this.imagePath,
    required this.isCollected,
    required this.line,
  });

  final String title;
  final String imagePath;
  final bool isCollected;
  final String line;
}
