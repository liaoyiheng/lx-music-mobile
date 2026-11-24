import { useRef, forwardRef, useImperativeHandle } from 'react'
import { TouchableOpacity, View } from 'react-native'

import LargeTitle from '@/components/apple/LargeTitle'
import Text from '@/components/common/Text'
import { useI18n } from '@/lang'
import { useStatusbarHeight } from '@/store/common/hook'
import { createStyle } from '@/utils/tools'
import { useApple, fontWeight, pagePadding, spacing } from '@/theme/apple'
import SourceSelector, { type SearchSource, type SearchSources } from '../SourceSelector'
import SearchInput, { type SearchInputType, type SearchInputProps } from './SearchInput'

export interface HeaderBarProps {
  isActive: boolean
  sourceList: SearchSources
  source: SearchSource
  onSourceChange: (source: SearchSource) => void
  onTipSearch: SearchInputProps['onChangeText']
  onSearch: SearchInputProps['onSubmit']
  onClear: NonNullable<SearchInputProps['onClear']>
  onCancel: () => void
  onFocus: NonNullable<SearchInputProps['onFocus']>
  onHideTipList: SearchInputProps['onBlur']
  onShowTipList: SearchInputProps['onTouchStart']
}

export interface HeaderBarType {
  setText: SearchInputType['setText']
  focus: SearchInputType['focus']
  blur: SearchInputType['blur']
}

/**
 * 搜索头部：
 * - 默认空态：LargeTitle「搜索」+ 右侧液态玻璃音源胶囊 + 独立圆角玻璃搜索框
 * - 聚焦或有输入关键词时：收起 LargeTitle，带 statusBarHeight 安全间距的聚焦搜索框 + 右侧红色「取消」按钮
 */
export default forwardRef<HeaderBarType, HeaderBarProps>(({
  isActive,
  sourceList,
  source,
  onSourceChange,
  onTipSearch,
  onSearch,
  onClear,
  onCancel,
  onFocus,
  onHideTipList,
  onShowTipList,
}, ref) => {
  const apple = useApple()
  const t = useI18n()
  const statusBarHeight = useStatusbarHeight()
  const searchInputRef = useRef<SearchInputType>(null)

  useImperativeHandle(ref, () => ({
    setText(text) {
      searchInputRef.current?.setText(text)
    },
    focus() {
      searchInputRef.current?.focus()
    },
    blur() {
      searchInputRef.current?.blur()
    },
  }), [])

  return (
    <View style={styles.wrapper}>
      {!isActive ? (
        <LargeTitle
          title={t('nav_search')}
          right={(
            <SourceSelector
              list={sourceList}
              source={source}
              onSourceChange={onSourceChange}
            />
          )}
        />
      ) : null}
      <View
        style={[
          styles.searchBarRow,
          isActive && { paddingTop: statusBarHeight + spacing.sm },
        ]}
      >
        <View style={styles.inputWrap}>
          <SearchInput
            ref={searchInputRef}
            onChangeText={onTipSearch}
            onSubmit={onSearch}
            onClear={onClear}
            onFocus={onFocus}
            onBlur={onHideTipList}
            onTouchStart={onShowTipList}
          />
        </View>
        {isActive ? (
          <TouchableOpacity
            activeOpacity={0.6}
            onPress={onCancel}
            style={styles.cancelBtn}
            hitSlop={{ top: 8, bottom: 8, left: 6, right: 8 }}
          >
            <Text size={16} color={apple.accent} style={styles.cancelText}>
              {t('apple_cancel')}
            </Text>
          </TouchableOpacity>
        ) : null}
      </View>
    </View>
  )
})

const styles = createStyle({
  wrapper: {
    zIndex: 3,
  },
  searchBarRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: pagePadding,
    paddingBottom: spacing.sm + 2,
    gap: 10,
  },
  inputWrap: {
    flex: 1,
    height: 40,
  },
  cancelBtn: {
    paddingVertical: 4,
    paddingHorizontal: 2,
    flexShrink: 0,
  },
  cancelText: {
    fontWeight: fontWeight.medium,
  },
})
