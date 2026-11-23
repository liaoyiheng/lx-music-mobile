import { memo, useMemo } from 'react'
import { StyleSheet, View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import { useSettingValue } from '@/store/setting/hook'
import { updateSetting } from '@/core/common'
import { useI18n } from '@/lang'
import { TRY_QUALITYS_LIST } from '@/core/music/utils'
import { spacing } from '@/theme/apple'

const useActive = (id: LX.Quality) => {
  const q = useSettingValue('player.playQuality')
  const isActive = useMemo(() => q == id, [q, id])
  return isActive
}

const Item = ({ id, name, showSeparator }: {
  id: LX.Quality
  name: string
  showSeparator: boolean
}) => {
  const isActive = useActive(id)
  return (
    <RadioRow
      label={name}
      selected={isActive}
      onPress={() => { updateSetting({ 'player.playQuality': id }) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()
  const playQualityList = useMemo(() => {
    return [...TRY_QUALITYS_LIST, '128k'].reverse() as LX.Quality[]
  }, [])

  return (
    <SubTitle title={t('setting_play_play_quality')} showSeparator={false}>
      <View style={styles.list}>
        {
          playQualityList.map((q, index) => (
            <Item
              name={q}
              id={q}
              key={q}
              showSeparator={index < playQualityList.length - 1}
            />
          ))
        }
      </View>
    </SubTitle>
  )
})

const styles = StyleSheet.create({
  list: {
    marginHorizontal: -spacing.lg,
    marginBottom: -spacing.md,
  },
})
