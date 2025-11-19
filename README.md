# Nettie Child Link Redirect

This GitHub Pages site powers QR code onboarding for Momma Nettie’s child app.

## 🔗 Purpose

When a parent scans a QR code from the guardian dashboard, it opens a browser-safe link like:
https://airnettie.github.io/link?token=abc123&guardianId=xyz456

This link instantly redirects to a deep link:
nettielink://child/link?token=abc123&guardianId=xyz456


If the child app is installed, it launches and links the device. If not, the parent is prompted to install it.

## 🧠 Why This Matters

- Works in any QR scanner or browser
- No custom domain required
- Enables secure, one-time linking between child and guardian
- Reinforces Nettie’s emotional promise: every scan is a handshake of trust

## 🛠 How It Works

- `link/index.html` contains a redirect to the deep link
- AndroidManifest.xml in the child app handles both `nettielink://` and `https://airnettie.github.io/link`
- QR codes embed the browser-safe link

## 💡 Next Steps

- Update QR generator to use `https://airnettie.github.io/link?...`
- Ensure child app is installed before scanning
- Celebrate every successful link as a milestone in emotional safety

---

Built with love and resilience by Corey Dotson, founder of Momma Nettie.
