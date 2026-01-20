# How to Upload to GitHub and Build APK Automatically

## Step 1: Create GitHub Account (if you don't have one)
Go to https://github.com and sign up for free.

## Step 2: Create New Repository

1. Go to https://github.com/new
2. **Repository name**: `second-brain-lite` (or any name you like)
3. **Description**: "Privacy-first Android app for capturing thoughts, lessons, and decisions"
4. **Visibility**: Choose **Private** (recommended) or Public
5. **DO NOT** check "Initialize with README" (we already have files)
6. Click **Create repository**

## Step 3: Upload Your Code

### Option A: Using GitHub Web Interface (Easiest)

1. After creating the repository, you'll see a page with instructions
2. Click "uploading an existing file" link
3. **Drag and drop** the entire `SecondBrainLite` folder contents
4. Or click "choose your files" and select all files
5. Add commit message: "Initial commit - Second Brain Lite Android app"
6. Click **Commit changes**

**Important**: Make sure to upload ALL files including:
- All `.kt` files
- All `.xml` files
- `build.gradle.kts` files
- `gradlew` and `gradlew.bat`
- `.github/workflows/build.yml` (very important!)

### Option B: Using Git Command Line

If you have Git installed:

```powershell
cd "c:\Users\pc\Desktop\2nd Brain\SecondBrainLite"

# Initialize git repository
git init

# Add all files
git add .

# Commit files
git commit -m "Initial commit - Second Brain Lite Android app"

# Add your GitHub repository (replace YOUR_USERNAME with your GitHub username)
git remote add origin https://github.com/YOUR_USERNAME/second-brain-lite.git

# Push to GitHub
git branch -M main
git push -u origin main
```

GitHub will ask for your credentials:
- **Username**: Your GitHub username
- **Password**: Use a **Personal Access Token** (not your password)
  - Create token at: https://github.com/settings/tokens
  - Select "repo" scope
  - Copy the token and use it as password

## Step 4: Build Happens Automatically!

Once you push the code:

1. GitHub Actions will **automatically start building** your APK
2. Go to your repository on GitHub
3. Click the **Actions** tab
4. You'll see "Build Android APK" workflow running
5. Wait 5-10 minutes for build to complete (first time takes longer)

## Step 5: Download Your APK

After the build succeeds:

1. Go to **Actions** tab
2. Click on the completed workflow run (green checkmark ✓)
3. Scroll down to **Artifacts** section
4. Click **app-debug** to download the APK
5. Extract the ZIP file
6. Install `app-debug.apk` on your Android device!

---

## Triggering New Builds

Every time you:
- Push new code to GitHub
- Or click "Run workflow" in Actions tab

GitHub will automatically build a fresh APK for you!

---

## Troubleshooting

### Build Fails?
1. Check the **Actions** logs for errors
2. Most common issues:
   - Missing files (make sure all files uploaded)
   - Gradle wrapper missing (make sure `gradlew` and `gradlew.bat` uploaded)

### Can't Upload Large Files?
GitHub has 100MB file limit. Our project should be well under that.

### Need Help?
The build logs in Actions tab will show exactly what went wrong.

---

## Summary

1. ✅ Create GitHub account
2. ✅ Create new repository  
3. ✅ Upload all project files
4. ✅ GitHub builds APK automatically
5. ✅ Download APK from Actions → Artifacts

**No Java or Android Studio needed on your computer!** 🎉
