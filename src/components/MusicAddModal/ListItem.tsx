import { TouchableOpacity, View } from 'react-native'
import { IonIcon } from '@/components/common/Icon'
import Text from '@/components/common/Text'
import { LIST_IDS } from '@/config/constant'
import { createStyle, toast } from '@/utils/tools'
import { useApple, fontWeight, hairline, radius } from '@/theme/apple'
import { useMusicExistsList } from '@/store/list/hook'

const getListIcon = (id: string) => {
  switch (id) {
    case LIST_IDS.LOVE: return 'heart'
    case LIST_IDS.DEFAULT: return 'musical-notes'
    default: return 'albums'
  }
}

export default ({ listInfo, onPress, musicInfo }: {
  listInfo: LX.List.MyListInfo
  onPress: (listInfo: LX.List.MyListInfo) => void
  musicInfo: LX.Music.MusicInfo
  width?: number
}) => {
  const apple = useApple()
  const isExists = useMusicExistsList(listInfo, musicInfo)

  const handlePress = () => {
    if (isExists) {
      toast(global.i18n.t('list_add_tip_exists'))
      return
    }
    onPress(listInfo)
  }

  return (
    <TouchableOpacity
      activeOpacity={0.65}
      style={[
        styles.row,
        {
          backgroundColor: apple.fill,
          opacity: isExists ? 0.5 : 1,
        },
      ]}
      onPress={handlePress}
    >
      <View
        style={[
          styles.iconBox,
          {
            backgroundColor: apple.accentFill,
            borderColor: apple.coverRing,
          },
        ]}
      >
        <IonIcon name={getListIcon(listInfo.id)} rawSize={18} color={apple.accent} />
      </View>
      <Text style={styles.name} numberOfLines={1} size={15} color={apple.label}>
        {listInfo.name}
      </Text>
      <IonIcon
        name={isExists ? 'checkmark-circle' : 'add-circle-outline'}
        rawSize={20}
        color={isExists ? apple.accent : apple.secondaryLabel}
      />
    </TouchableOpacity>
  )
}

export const styles = createStyle({
  row: {
    minHeight: 54,
    paddingHorizontal: 12,
    paddingVertical: 9,
    borderRadius: radius.large,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 12,
  },
  iconBox: {
    width: 36,
    height: 36,
    borderRadius: 9,
    borderWidth: hairline,
    alignItems: 'center',
    justifyContent: 'center',
  },
  name: {
    flex: 1,
    fontWeight: fontWeight.medium,
  },
})
