docker run -t -i -v $(pwd):/mnt -e GITHUB_TOKEN cinterloper/lash bash -c 'cd /mnt/; bats workflow/build.bats; bats workflow/publish.bats; bats workflow/clean.bats'

