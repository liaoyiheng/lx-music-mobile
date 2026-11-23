import { memo, useCallback, useMemo, useRef } from 'react'
import { View } from 'react-native'

import SubTitle from '../../components/SubTitle'
import { RadioRow } from '@/components/apple/RadioRow'
import { createStyle } from '@/utils/tools'
import { setApiSource } from '@/core/apiSource'
import { useI18n } from '@/lang'
import apiSourceInfo from '@/utils/musicSdk/api-source-info'
import { useSettingValue } from '@/store/setting/hook'
import { useStatus, useUserApiList } from '@/store/userApi'
import Button from '../../components/Button'
import UserApiEditModal, { type UserApiEditModalType } from './UserApiEditModal'
import { spacing } from '@/theme/apple'

const apiSourceList = apiSourceInfo.map(api => ({
  id: api.id,
  name: api.name,
  disabled: api.disabled,
}))

const useActive = (id: string) => {
  const activeLangId = useSettingValue('common.apiSource')
  const isActive = useMemo(() => activeLangId == id, [activeLangId, id])
  return isActive
}

const Item = ({ id, name, desc, statusLabel, change, showSeparator }: {
  id: string
  name: string
  desc?: string
  statusLabel?: string
  change: (id: string) => void
  showSeparator: boolean
}) => {
  const isActive = useActive(id)
  const description = [desc, statusLabel].filter(Boolean).join('  ') || undefined

  return (
    <RadioRow
      label={name}
      description={description}
      selected={isActive}
      onPress={() => { change(id) }}
      showSeparator={showSeparator}
    />
  )
}

export default memo(() => {
  const t = useI18n()
  const list = useMemo(() => apiSourceList.map(s => ({
    // @ts-expect-error
    name: t(`setting_basic_source_${s.id}`) || s.name,
    id: s.id,
  })), [t])
  const setApiSourceId = useCallback((id: string) => {
    setApiSource(id)
  }, [])
  const userApiListRaw = useUserApiList()
  const apiStatus = useStatus()
  const apiSourceSetting = useSettingValue('common.apiSource')
  const userApiList = useMemo(() => {
    const getApiStatus = () => {
      let status
      if (apiStatus.status) status = t('setting_basic_source_status_success')
      else if (apiStatus.message == 'initing') status = t('setting_basic_source_status_initing')
      else status = t('setting_basic_source_status_failed')

      return status
    }
    return userApiListRaw.map(api => {
      const statusLabel = api.id == apiSourceSetting ? `[${getApiStatus()}]` : ''
      return {
        id: api.id,
        name: api.name,
        label: `${api.name}${statusLabel}`,
        desc: [/^\d/.test(api.version) ? `v${api.version}` : api.version].filter(Boolean).join(', '),
        statusLabel,
      }
    })
  }, [userApiListRaw, apiStatus, apiSourceSetting, t])

  const modalRef = useRef<UserApiEditModalType>(null)
  const handleShow = () => {
    modalRef.current?.show()
  }

  const totalCount = list.length + userApiList.length

  return (
    <SubTitle title={t('setting_basic_source')}>
      <View style={styles.list}>
        {
          list.map(({ id, name }, index) => (
            <Item
              name={name}
              id={id}
              key={id}
              change={setApiSourceId}
              showSeparator={index < totalCount - 1}
            />
          ))
        }
        {
          userApiList.map(({ id, name, desc, statusLabel }, index) => (
            <Item
              name={name}
              desc={desc}
              statusLabel={statusLabel}
              id={id}
              key={id}
              change={setApiSourceId}
              showSeparator={list.length + index < totalCount - 1}
            />
          ))
        }
      </View>
      <View style={styles.btn}>
        <Button onPress={handleShow}>{t('setting_basic_source_user_api_btn')}</Button>
      </View>
      <UserApiEditModal ref={modalRef} />
    </SubTitle>
  )
})

const styles = createStyle({
  list: {
    marginHorizontal: -spacing.lg,
  },
  btn: {
    marginTop: spacing.sm,
    flexDirection: 'row',
  },
})
