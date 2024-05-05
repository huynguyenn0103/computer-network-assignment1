# BitTorrent Application

## Description:
Welcome to the BitTorrent Application README. This comprehensive guide will walk you through the features, usage, setup, and more of our BitTorrent application. Our application encompasses a BitTorrent client, a tracker server, and a monitoring web application, all implemented in Java using Spring framework and socket programming.

## Features:

1. **BitTorrent Client:**
   - The BitTorrent client component allows users to both download and upload files using the BitTorrent protocol.
   - It supports concurrent downloading from multiple peers, significantly enhancing download speeds.
   - Simultaneously, it enables users to upload multiple files to multiple peers, facilitating efficient sharing.

2. **Tracker Server:**
   - The tracker server serves as a crucial intermediary between peers, keeping track of the availability of files and the peers possessing them.
   - It facilitates peer discovery and communication, enabling efficient file transfers.

3. **Monitor Web Application:**
   - The monitoring web application provides users with an intuitive graphical interface to monitor the status of their downloads and uploads.
   - Users can track download progress, monitor active peers, and manage their file-sharing activities conveniently through this interface.

## Usage:

1. **Commands:**
   - **info filename.torrent:** This command allows users to parse a specified torrent file using our custom bencode implementation. Unlike many BitTorrent clients that rely on external libraries for parsing, our implementation is self-contained and independent.
   
   - **download filename.torrent:** Initiate the download of a specified torrent file. This command supports the download of multiple files concurrently.

2. **Multi-threaded Operation:**
   - Our application harnesses the power of multithreading to enable simultaneous downloading from and uploading to multiple peers.
   - Multithreading enhances efficiency and maximizes bandwidth utilization, resulting in faster transfer speeds and improved user experience.

## Getting Started:

1. **Prerequisites:**
   - Ensure you have Java Development Kit (JDK) installed on your system.
   - Install the Spring framework to run the application.
   - Familiarize yourself with basic socket programming concepts.

2. **Setup:**
   - Clone the repository from our GitHub repository.
   - Navigate to the project directory and compile the source code.
   - Start the application by running the provided scripts or manually launching the necessary components.

3. **Accessing the Monitoring Web Application:**
   - Once the application is running, access the monitoring web application through your preferred web browser.
   - The web application interface provides real-time insights into download and upload activities, enabling users to manage their file-sharing operations effectively.

## Dependencies:
- Java Development Kit (JDK)
- Spring Framework
- Socket Programming Libraries

## Contributing:
We welcome contributions to our project. Fork the repository, make your enhancements or bug fixes, and submit pull requests for review.

## License:
This project is licensed under the Nguyen Duc Bao Huy & Dang Hoang Gia License. Refer to the LICENSE file for more details.

## Contact:
For inquiries or support, please contact us at huy.nguyenducbao2003@hcmut.edu.vn.

## Acknowledgments:
We extend our gratitude to the developers of the BitTorrent protocol and any third-party libraries or resources utilized in this project. Thank you for your contributions to open-source software development.
