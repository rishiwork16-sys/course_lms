# Free Deployment Guide for Final LMS

Is project ko free mein live karne ke liye hum **Render** (App hosting ke liye) aur **TiDB Cloud** (MySQL Database ke liye) use karenge. Yeh dono platforms free tier offer karte hain.

## Step 1: Database Setup (TiDB Cloud)
Chunki aapka project MySQL use kar raha hai, humein ek free MySQL database chahiye.

1. **TiDB Cloud** par jaayein: [https://tidbcloud.com/](https://tidbcloud.com/)
2. Sign up karein aur ek naya **Serverless Tier** cluster create karein (Free).
3. Cluster create hone ke baad, **"Connect"** par click karein.
4. "Connect with" mein **"General"** ya **"JDBC"** select karein.
5. Wahan se aapko ye details milengi:
   - **Host** (e.g., `gateway01....tidbcloud.com`)
   - **Port** (usually `4000`)
   - **User** (e.g., `2User.root`)
   - **Password** (Jo aapne set kiya ho)
   - **Database Name** (default `test` hota hai, aap ise `final_lms` kar sakte hain connection string mein).

## Step 2: GitHub par Code Upload Karein
Agar aapne abhi tak code GitHub par nahi dala hai:
1. GitHub par ek new repository banayein (Private rakhein toh behtar hai kyunki keys hardcoded hain).
2. Apne computer se code push karein:
   ```bash
   git init
   git add .
   git commit -m "Initial commit"
   git branch -M main
   git remote add origin <YOUR_GITHUB_REPO_URL>
   git push -u origin main
   ```

## Step 3: Render par Deploy Karein
1. **Render** par jaayein: [https://render.com/](https://render.com/)
2. Sign up/Login karein.
3. **"New +"** button dabayein aur **"Web Service"** select karein.
4. "Connect a repository" mein apna GitHub repo select karein.
5. **Settings:**
   - **Name:** Kuch bhi (e.g., `finallms-live`)
   - **Region:** Singapore (India ke paas)
   - **Runtime:** **Docker** (Bahut important!)
   - **Instance Type:** Free
6. **Environment Variables** (Advanced section mein):
   Yahan aapko wo values daalni hain jo `application.properties` mein hain, taaki live server par sahi database connect ho. Add karein:

   | Key | Value |
   | --- | --- |
   | `DB_URL` | `jdbc:mysql://<TiDB_HOST>:4000/final_lms?sslMode=VERIFY_IDENTITY&enabledTLSProtocols=TLSv1.2,TLSv1.3` (TiDB se copy karein) |
   | `DB_USER` | TiDB Username |
   | `DB_PASS` | TiDB Password |
   | `JWT_SECRET` | Koi bhi lamba random string (Security ke liye) |
   | `RAZORPAY_KEY_ID` | Aapki Razorpay Key ID |
   | `RAZORPAY_KEY_SECRET` | Aapka Razorpay Secret |
   | `AWS_ACCESS_KEY` | Aapki AWS Access Key |
   | `AWS_SECRET_KEY` | Aapki AWS Secret Key |

   *Note: Agar aap AWS S3 use nahi karna chahte toh code mein changes karne padenge, warna upload fail ho sakta hai.*

7. **"Create Web Service"** par click karein.

## Step 4: Verification
Render thoda time lega (5-10 mins) Docker image build karne mein. Logs mein dekhein "Build Successful" aur "Started Application".
Jab deploy ho jaye, Render aapko ek URL dega (e.g., `https://finallms-live.onrender.com`). Uspe click karke check karein.

---
**Important Note:** Aapke `application.properties` mein keys hardcoded hain. GitHub par Public repo banane se pehle unhe hata dein ya Private repo use karein.
