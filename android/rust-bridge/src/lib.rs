//! JNI adapter for VectorCraft's original geometry kernel.
use jni::objects::{JClass, JFloatArray};
use jni::sys::{jfloat, jfloatArray, jstring};
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
    if env.set_float_array_region(&result, 0, &clipped).is_err() { return std::ptr::null_mut(); }
    result.into_raw()
}

/// Export vector rectangles to SVG using VectorCraft's geometry kernel.
/// Each four floats are two opposite corners (x0, y0, x1, y1).
#[unsafe(no_mangle)]
pub extern "system" fn Java_org_vectorcraft_android_MainActivity_00024Companion_exportSvg(
    mut env: JNIEnv, _class: JClass, data: JFloatArray, width: jfloat, height: jfloat,
) -> jstring {
    let Ok(len) = env.get_array_length(&data) else { return std::ptr::null_mut(); };
    let mut values = vec![0.0f32; len as usize];
    if env.get_float_array_region(&data, 0, &mut values).is_err() { return std::ptr::null_mut(); }
    let w = width.max(1.0);
    let h = height.max(1.0);
    let mut output = format!("<svg xmlns=\"http://www.w3.org/2000/svg\" width=\"{w}\" height=\"{h}\" viewBox=\"0 0 {w} {h}\">\n");
    for coords in values.chunks_exact(4) {
        if !coords.iter().all(|f| f.is_finite()) { continue; }
        let x0 = coords[0].min(coords[2]);
        let y0 = coords[1].min(coords[3]);
        let x1 = coords[0].max(coords[2]);
        let y1 = coords[1].max(coords[3]);
        let rect = Rect::new(f64::from(x0), f64::from(y0), f64::from(x1), f64::from(y1));
        if rect.x1 <= rect.x0 || rect.y1 <= rect.y0 { continue; }
        output.push_str(&format!("<rect x=\"{}\" y=\"{}\" width=\"{}\" height=\"{}\" fill=\"#2D65C4\" fill-opacity=\"0.18\" stroke=\"#2D65C4\" stroke-width=\"2\"/>\n", rect.x0, rect.y0, rect.x1-rect.x0, rect.y1-rect.y0));
    }
    output.push_str("</svg>\n");
    match env.new_string(output) { Ok(s) => s.into_raw(), Err(_) => std::ptr::null_mut() }
}
