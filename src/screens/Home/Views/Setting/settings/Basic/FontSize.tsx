import { memo, useMemo } from 'react'
import { StyleSheet, View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import { useI18n } from '@/lang'
import { setFontSize } from '@/core/common'
import { useFontSize } from '@/store/common/hook'
import Text from '@/components/common/Text'
import { getTextSize } from '@/utils/pixelRatio'
import { useApple, radius, spacing } from '@/theme/apple'

const LIST = [
  {
    size: 0.8,
    name: 'setting_basic_font_size_80',
  },
  {
    size: 0.9,
    name: 'setting_basic_font_size_90',
  },
  {
    size: 1,
    name: 'setting_basic_font_size_100',
  },
  {
    size: 1.1,
    name: 'setting_basic_font_size_110',
  },
  {
    size: 1.2,
    name: 'setting_basic_font_size_120',
  },
  {
    size: 1.3,
    name: 'setting_basic_font_size_130',
  },
] as const

type SIZE_TYPE = typeof LIST[number]['size']

const useActive = (size: SIZE_TYPE) => {
  const _size = useFontSize()
  const isActive = useMemo(() => _size == size, [_size, size])
  return isActive
}

const SizeText = () => {
  const size = getTextSize(14) * useFontSize()
  const t = useI18n()
  const apple = useApple()

  return <Text style={{ fontSize: size }} color={apple.label}>{t('setting_basic_font_size_preview')}</Text>
}

const Item = ({ size, label, showSeparator }: {
  size: SIZE_TYPE
  label: string
  showSeparator: boolean
}) => {
  const isActive = useActive(size)
  return (
    <RadioRow
      label={label}
      selected={isActive}
      onPress={() => { setFontSize(size) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()
  const apple = useApple()

  const list = useMemo(() => {
    return LIST.map((item) => ({ size: item.size, name: t(item.name) }))
  }, [t])

  return (
    <SubTitle title={t('setting_basic_font_size')}>
      <View style={{ ...styles.preview, backgroundColor: apple.fill }}>
        <SizeText />
      </View>
      <View style={styles.list}>
        {
          list.map(({ size, name }, index) => (
            <Item
              key={size}
              size={size}
              label={name}
              showSeparator={index < list.length - 1}
            />
          ))
        }
      </View>
    </SubTitle>
  )
})

const styles = StyleSheet.create({
  preview: {
    justifyContent: 'center',
    paddingHorizontal: spacing.md,
    paddingVertical: spacing.sm,
    minHeight: 46,
    borderRadius: radius.medium,
    marginBottom: spacing.xs,
  },
  list: {
    marginHorizontal: -spacing.lg,
    marginBottom: -spacing.md,
  },
})
