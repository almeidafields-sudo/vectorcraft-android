//! First JNI boundary into the *actual* VectorCraft geometry kernel.
//! Prototype only: the Android Canvas below is temporary, NOT the egui interface.
use jni::objects::{JClass, JFloatArray};
use jni::sys::{jfloat, jfloatArray};
use jni::JNIEnv;
use vectorcraft_geom::{Point, Rect};

#[unsafe(no_mangle)]
pub extern "system" fn Java_org_vectorcraft_android_MainActivity_00024Companion_clipPoint(
    mut env: JNIEnv, _class: JClass, x: jfloat, y: jfloat, width: jfloat, height: jfloat,
) -> jfloatArray {
    let bounds = Rect::new(0.0, 0.0, f64::from(width.max(0.0)), f64::from(height.max(0.0)));
    let point = Point::new(f64::from(x), f64::from(y));
    let clipped = [point.x.clamp(bounds.x0, bounds.x1) as f32,
                   point.y.clamp(bounds.y0, bounds.y1) as f32];
    let Ok(result) = env.new_float_array(2) else { return std::ptr::null_mut() };
    if env.set_float_array_region(&result, 0, &clipped).is_err() {
        return std::ptr::null_mut();
    }
    result.into_raw()
}
