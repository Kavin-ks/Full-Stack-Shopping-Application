import puppeteer from 'puppeteer-core'
import path from 'path'
import fs from 'fs'

const CHROME_PATH = '/Applications/Google Chrome.app/Contents/MacOS/Google Chrome'

async function run() {
  const browser = await puppeteer.launch({
    executablePath: CHROME_PATH,
    headless: true,
    args: ['--no-sandbox', '--disable-setuid-sandbox', '--window-size=1200,800']
  })

  const page = await browser.newPage()
  await page.setViewport({ width: 1200, height: 800, deviceScaleFactor: 2 })

  // 1. Initial Product Catalog
  await page.goto('http://localhost:5176', { waitUntil: 'networkidle0' })
  await new Promise(r => setTimeout(r, 600))
  await page.screenshot({ path: 'extracted_images/fig_p5_1.png' })
  console.log('Saved fig_p5_1.png')

  // 2. Filter & Add to Cart Toast
  const buttons = await page.$$('.pill-btn')
  if (buttons.length > 2) {
    await buttons[2].click() // Click 'Audio' or second category
  }
  await new Promise(r => setTimeout(r, 300))
  const addBtn = await page.$('.add-btn')
  if (addBtn) {
    await addBtn.click()
  }
  await new Promise(r => setTimeout(r, 400))
  await page.screenshot({ path: 'extracted_images/fig_p5_2.png' })
  console.log('Saved fig_p5_2.png')

  // 3. Shopping Cart View
  const cartNavBtn = await page.$('.cart-btn')
  if (cartNavBtn) {
    await cartNavBtn.click()
  }
  await new Promise(r => setTimeout(r, 600))
  await page.screenshot({ path: 'extracted_images/fig_p6_1.png' })
  console.log('Saved fig_p6_1.png')

  // 4. Cart Quantity Update
  const qtyPlusBtns = await page.$$('.qty-btn')
  if (qtyPlusBtns.length > 1) {
    await qtyPlusBtns[1].click() // Click + on first item
  }
  await new Promise(r => setTimeout(r, 500))
  await page.screenshot({ path: 'extracted_images/fig_p6_2.png' })
  console.log('Saved fig_p6_2.png')

  await browser.close()
  console.log('All screenshots captured successfully!')
}

run().catch(console.error)
