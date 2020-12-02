package com.example.firewarning.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.NotificationCompat;
import androidx.databinding.DataBindingUtil;
import androidx.databinding.ViewDataBinding;
import androidx.recyclerview.widget.RecyclerView;

import com.example.firewarning.BR;
import com.example.firewarning.MainActivity;
import com.example.firewarning.R;
import com.example.firewarning.dao.AppDatabase;
import com.example.firewarning.ui.device.model.Device;
import com.example.firewarning.ui.gallery.Image;
import com.google.firebase.storage.FirebaseStorage;
import com.suke.widget.SwitchButton;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import io.reactivex.disposables.CompositeDisposable;

public class BaseBindingAdapter<T> extends RecyclerView.Adapter<BaseBindingAdapter.ViewHolder> {
    private static final int EMPTY_VIEW = 10;
    private static final int MAX_IMAGE_NUM = 22;
    private static final String RESTAURANT_URL_FMT = "https://storage.googleapis.com/firestorequickstarts.appspot.com/food_%d.png";

    private List<T> data;
    private LayoutInflater inflater;
    private @LayoutRes
    int resId;
    private OnItemClickListener<T> onItemClickListener;
    private Context mContext;
    private FirebaseStorage mStorage;
    private CompositeDisposable disposable = new CompositeDisposable();
    private List<Image> images;

    public void setOnItemClickListener(OnItemClickListener<T> onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public BaseBindingAdapter(Context context, @LayoutRes int resId) {
        inflater = LayoutInflater.from(context);
        this.resId = resId;
        mContext = context;
        images = AppDatabase.getDatabase(mContext).imageDAO().getAllImages();
    }

    public void setData(List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return data;
    }

    @NonNull
    @Override
    public BaseBindingAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(DataBindingUtil.inflate(inflater, resId, parent, false));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onBindViewHolder(@NonNull BaseBindingAdapter.ViewHolder holder, int position) {
        T item = data.get(position);
        holder.binding.setVariable(BR.item_device, item);
        holder.binding.executePendingBindings();
        holder.itemView.setOnClickListener(v -> {
            onItemClickListener.onItemClick(item);
        });

        holder.itemView.setOnLongClickListener(v -> {
            onItemClickListener.onItemLongClick(item);
            return false;
        });

        TextView myText = (TextView) holder.itemView.findViewById(R.id.txtDeviceName);

        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(50); //You can manage the blinking time with this parameter
        anim.setStartOffset(20);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);

        if (item instanceof Device) {
            Device device = (Device) item;
            try {
                AppCompatImageView imgView = holder.itemView.findViewById(R.id.imgDevice);
                if (!CommonActivity.isNullOrEmpty(device.getId())) {
                    images.forEach(image -> {
                        if (device.getId().equals(image.getDeviceId())) {
                            try {
                                Bitmap bitmap = BitmapFactory.decodeByteArray(image.getImage(), 0, image.getImage().length);
                                imgView.setImageBitmap(bitmap);
                                device.setBitmapImage(image.getImage());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }

                holder.itemView.findViewById(R.id.imgEdit).setOnClickListener(v
                        -> onItemClickListener.onBtnEditClick(item, position));
                SwitchButton switchButton = holder.itemView.findViewById(R.id.onOffSwitch);
                switchButton.setOnCheckedChangeListener((view, isChecked) -> {
                    if (isChecked) {
                        holder.itemView.findViewById(R.id.progressChangeStatus).setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> {
                            holder.itemView.findViewById(R.id.progressChangeStatus).setVisibility(View.GONE);
                            holder.itemView.findViewById(R.id.txtTemp).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.txtOn).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.txtOff).setVisibility(View.GONE);

                            holder.itemView.findViewById(R.id.off).setVisibility(View.GONE);
                            holder.itemView.findViewById(R.id.off).animate().alpha(0.0f);

                            holder.itemView.findViewById(R.id.on).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.on).animate().alpha(1.0f);
                        }, 1000);
                    } else {
                        holder.itemView.findViewById(R.id.progressChangeStatus).setVisibility(View.VISIBLE);
                        new Handler().postDelayed(() -> {
                            holder.itemView.findViewById(R.id.progressChangeStatus).setVisibility(View.GONE);
                            holder.itemView.findViewById(R.id.txtTemp).setVisibility(View.GONE);
                            holder.itemView.findViewById(R.id.txtOn).setVisibility(View.GONE);
                            holder.itemView.findViewById(R.id.txtOff).setVisibility(View.VISIBLE);

                            holder.itemView.findViewById(R.id.on).setVisibility(View.GONE);
                            holder.itemView.findViewById(R.id.on).animate().alpha(0.0f);

                            holder.itemView.findViewById(R.id.off).setVisibility(View.VISIBLE);
                            holder.itemView.findViewById(R.id.off).animate().alpha(1.0f);
                        }, 1000);
                    }
                });
                if (!CommonActivity.isNullOrEmpty(device.getNO())
                        && !CommonActivity.isNullOrEmpty(device.getNG())) {
                    try {
                        if (Double.parseDouble(device.getNO()) > Double.parseDouble(device.getNG())) {
                            myText.setTextColor(mContext.getResources().getColor(R.color.red));
                            myText.startAnimation(anim);
                        } else {
                            myText.setTextColor(mContext.getResources().getColor(R.color.black));
                            myText.clearAnimation();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadBase64Image() {
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    public int getItemViewType(int position) {
//        try {
//            Device device = (Device) data.get(position);
//            if (CommonActivity.isNullOrEmpty(device.getId())) {
//                return EMPTY_VIEW;
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return super.getItemViewType(position);
//    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ViewDataBinding binding;

        public ViewHolder(ViewDataBinding inflate) {
            super(inflate.getRoot());
            this.binding = inflate;
        }
    }

    private void createNotification(String ng, String idDevice) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(Objects.requireNonNull(mContext)
                        .getApplicationContext(), "notify_001");
        Intent ii = new Intent(mContext.getApplicationContext(), MainActivity.class);
        ii.putExtra("menuFragment", "DetailDeviceFragment");
        ii.putExtra("idDevice", idDevice);
        PendingIntent pendingIntent = PendingIntent.getActivity(mContext, 0, ii, 0);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText("Nhiệt độ đo được: " + ng + " tại thiết bị: " + idDevice);
        bigText.setBigContentTitle("Nhiệt độ vượt ngưỡng !");
        bigText.setSummaryText("Cảnh báo");

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.ic_warning_red);
        mBuilder.setContentTitle(mContext.getString(R.string.app_name));
        mBuilder.setContentText("Nhiệt độ vượt ngưỡng !");
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);

        NotificationManager mNotificationManager
                = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

// === Removed some obsoletes
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "Your_channel_id";
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
            mBuilder.setChannelId(channelId);
        }

        if (mNotificationManager != null) {
            mNotificationManager.notify(0, mBuilder.build());
        }
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item);

        void onBtnEditClick(T item, int position);

        void onItemLongClick(T item);
    }

    private static String getRandomImageUrl(Random random) {
        // Integer between 1 and MAX_IMAGE_NUM (inclusive)
        int id = random.nextInt(MAX_IMAGE_NUM) + 1;

        return String.format(Locale.getDefault(), RESTAURANT_URL_FMT, id);
    }
}

