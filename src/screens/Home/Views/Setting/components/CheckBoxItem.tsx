import { memo } from 'react'
import { SwitchRow } from '@/components/apple/SwitchRow'
import type { CheckBoxProps } from '@/components/common/CheckBox'

export interface CheckBoxItemProps extends CheckBoxProps {
  description?: string
  showSeparator?: boolean
  horizontalPadding?: number
}

export default memo(({
  check,
  label,
  onChange,
  disabled,
  helpDesc,
  description,
  showSeparator = true,
  horizontalPadding = 16,
}: CheckBoxItemProps) => {
  return (
    <SwitchRow
      label={label ?? ''}
      description={description ?? helpDesc}
      value={check}
      onValueChange={onChange}
      disabled={disabled}
      showSeparator={showSeparator}
      horizontalPadding={horizontalPadding}
    />
  )
})
