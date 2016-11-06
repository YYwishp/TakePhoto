package com.jph.simple;

import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.jph.takephoto.app.TakePhoto;
import com.jph.takephoto.compress.CompressConfig;
import com.jph.takephoto.model.CropOptions;
import com.jph.takephoto.model.LuBanOptions;
import com.jph.takephoto.model.TakePhotoOptions;

import java.io.File;

import me.shaohui.advancedluban.Luban;

/**
 * - 支持通过相机拍照获取图片
 * - 支持从相册选择图片
 * - 支持从文件选择图片
 * - 支持多图选择
 * - 支持批量图片裁切
 * - 支持批量图片压缩
 * - 支持对图片进行压缩
 * - 支持对图片进行裁剪
 * - 支持对裁剪及压缩参数自定义
 * - 提供自带裁剪工具(可选)
 * - 支持智能选取及裁剪异常处理
 * - 支持因拍照Activity被回收后的自动恢复
 * Author: crazycodeboy
 * Date: 2016/9/21 0007 20:10
 * Version:4.0.0
 * 技术博文：http://www.cboy.me
 * GitHub:https://github.com/crazycodeboy
 * Eamil:crazycodeboy@gmail.com
 */
public class CustomHelper {
	private View rootView;
	private RadioGroup rgCrop, rgCompress, rgFrom, rgCropSize, rgCropTool, rgShowProgressBar, rgPickTool, rgCompressTool;
	private EditText etCropHeight, etCropWidth, etLimit, etSize, etHeightPx, etWidthPx;

	private CustomHelper(View rootView) {
		this.rootView = rootView;
		init();
	}

	public static CustomHelper of(View rootView) {
		return new CustomHelper(rootView);
	}

	private void init() {
		//是否剪裁
		rgCrop = (RadioGroup) rootView.findViewById(R.id.rgCrop);
		//是否压缩
		rgCompress = (RadioGroup) rootView.findViewById(R.id.rgCompress);
		//压缩工具
		rgCompressTool = (RadioGroup) rootView.findViewById(R.id.rgCompressTool);
		//宽高选择
		rgCropSize = (RadioGroup) rootView.findViewById(R.id.rgCropSize);
		//从哪里选择图片
		rgFrom = (RadioGroup) rootView.findViewById(R.id.rgFrom);
		//使用TakePhoto自带相册
		rgPickTool = (RadioGroup) rootView.findViewById(R.id.rgPickTool);
		//是否显示进度条
		rgShowProgressBar = (RadioGroup) rootView.findViewById(R.id.rgShowProgressBar);
		//剪裁工具
		rgCropTool = (RadioGroup) rootView.findViewById(R.id.rgCropTool);
		//比例 高度
		etCropHeight = (EditText) rootView.findViewById(R.id.etCropHeight);
		//比例 宽度
		etCropWidth = (EditText) rootView.findViewById(R.id.etCropWidth);
		//数量
		etLimit = (EditText) rootView.findViewById(R.id.etLimit);
		//大小不超过
		etSize = (EditText) rootView.findViewById(R.id.etSize);
		etHeightPx = (EditText) rootView.findViewById(R.id.etHeightPx);
		etWidthPx = (EditText) rootView.findViewById(R.id.etWidthPx);
	}

	/**
	 * 点击事件
	 *
	 * @param view
	 * @param takePhoto 库的一个接口
	 */
	public void onClick(View view, TakePhoto takePhoto) {
		File file = new File(Environment.getExternalStorageDirectory(), "/temp/" + System.currentTimeMillis() + ".jpg");
		if (!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		Uri imageUri = Uri.fromFile(file);
		//压缩图片
		configCompress(takePhoto);
		//是否使用takephopo自带的相册
		configTakePhotoOpthion(takePhoto);
		//判断店家的是什么控件
		switch (view.getId()) {
			//选择照片
			case R.id.btnPickBySelect:
				//数量
				int limit = Integer.parseInt(etLimit.getText().toString());
				if (limit > 1) {
					//是否剪裁
					if (rgCrop.getCheckedRadioButtonId() == R.id.rbCropYes) {
						takePhoto.onPickMultipleWithCrop(limit, getCropOptions());
					} else {
						takePhoto.onPickMultiple(limit);
					}
					return;
				}
				if (rgFrom.getCheckedRadioButtonId() == R.id.rbFile) {
					if (rgCrop.getCheckedRadioButtonId() == R.id.rbCropYes) {
						takePhoto.onPickFromDocumentsWithCrop(imageUri, getCropOptions());
					} else {
						takePhoto.onPickFromDocuments();
					}
					return;
				} else {
					if (rgCrop.getCheckedRadioButtonId() == R.id.rbCropYes) {
						takePhoto.onPickFromGalleryWithCrop(imageUri, getCropOptions());
					} else {
						takePhoto.onPickFromGallery();
					}
				}
				break;
			//拍照
			case R.id.btnPickByTake:
				//是否剪裁
				if (rgCrop.getCheckedRadioButtonId() == R.id.rbCropYes) {
					takePhoto.onPickFromCaptureWithCrop(imageUri, getCropOptions());
				} else {
					takePhoto.onPickFromCapture(imageUri);
				}
				break;
			default:
				break;
		}
	}

	/**
	 * 判断是不是使用takephoto自己的相册
	 * @param takePhoto
	 */
	private void configTakePhotoOpthion(TakePhoto takePhoto) {
		//是否使用takephoto自带相册
		if (rgPickTool.getCheckedRadioButtonId() == R.id.rbPickWithOwn) {
			takePhoto.setTakePhotoOptions(new TakePhotoOptions.Builder().setWithOwnGallery(true).create());
		}
	}

	/**
	 * 压缩图片
	 * @param takePhoto
	 */
	private void configCompress(TakePhoto takePhoto) {
		//是否压缩
		if (rgCompress.getCheckedRadioButtonId() != R.id.rbCompressYes) {
			takePhoto.onEnableCompress(null, false);
			return;
		}
		//大小不超过
		int maxSize = Integer.parseInt(etSize.getText().toString());
		int width = Integer.parseInt(etCropWidth.getText().toString());
		int height = Integer.parseInt(etHeightPx.getText().toString());
		//是否显示进度条
		boolean showProgressBar = rgShowProgressBar.getCheckedRadioButtonId() == R.id.rbShowYes ? true : false;
		CompressConfig config;
		//压缩工具是自带的
		if (rgCompressTool.getCheckedRadioButtonId() == R.id.rbCompressWithOwn) {
			config = new CompressConfig.Builder()
					.setMaxSize(maxSize)
					.setMaxPixel(width >= height ? width : height)
					.create();
		} else {
			//不自带的压缩工具，使用鲁班的
			LuBanOptions option = new LuBanOptions.Builder()
					.setGear(Luban.CUSTOM_GEAR)
					.setMaxHeight(height)
					.setMaxWidth(width)
					.setMaxSize(maxSize)
					.create();
			config = CompressConfig.ofLuban(option);
		}
		//最终设置进行压缩
		takePhoto.onEnableCompress(config, showProgressBar);
	}

	//获取裁剪参数
	private CropOptions getCropOptions() {
		//不剪裁
		if (rgCrop.getCheckedRadioButtonId() != R.id.rbCropYes) {
			return null;
		}
		//剪裁
		int height = Integer.parseInt(etCropHeight.getText().toString());
		int width = Integer.parseInt(etCropWidth.getText().toString());
		//是否自带剪裁工具
		boolean withWonCrop = rgCropTool.getCheckedRadioButtonId() == R.id.rbCropOwn ? true : false;
		CropOptions.Builder builder = new CropOptions.Builder();
		//检查选择尺寸
		if (rgCropSize.getCheckedRadioButtonId() == R.id.rbAspect) {
			builder.setAspectX(width).setAspectY(height);
		} else {
			builder.setOutputX(width).setOutputY(height);
		}
		builder.setWithOwnCrop(withWonCrop);
		return builder.create();
	}
}
